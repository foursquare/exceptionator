// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{BucketActions, IndexActions, SaveResult}
import com.foursquare.exceptionator.model.io.{BacktraceLine, BucketId, Incoming, Outgoing}
import com.foursquare.exceptionator.model.{MongoOutgoing, BucketRecord, BucketRecordHistogram, NoticeRecord}
import com.foursquare.exceptionator.util.{Config, Hash, PluginLoader, RegexUtil, Logger}
import org.bson.types.ObjectId
import net.liftweb.json._
import org.joda.time.DateTime
import scalaj.collection.Imports._
import com.foursquare.rogue.LiftRogue._
import com.twitter.ostrich.stats.Stats
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern



class ConcreteBucketActions extends BucketActions with IndexActions with Logger {
  var currentTime: Long = 0
  var lastHistogramTrim: Long = 0


  def ensureIndexes {
    List(BucketRecord, BucketRecordHistogram).foreach(metaRecord => {
        metaRecord.mongoIndexList.foreach(i =>
          metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  def get(ids: List[String], noticesPerBucketLimit: Option[Int], now: DateTime): List[Outgoing] = {
    val buckets = BucketRecord.where(_._id in ids).fetch
    val noticeIds = buckets.flatMap(bucket => {
      noticesPerBucketLimit match {
        case Some(limit) => bucket.notices.value.takeRight(limit)
        case None => bucket.notices.value
      }
    }).toSet

    def monthFmt(t: DateTime) = Hash.fieldNameEncode(t.getMonthOfYear)
    def dayFmt(t: DateTime) = Hash.fieldNameEncode(t.getMonthOfYear) + Hash.fieldNameEncode(t.getDayOfMonth)
    def hourFmt(t: DateTime) = {
      Hash.fieldNameEncode(t.getMonthOfYear) +
      Hash.fieldNameEncode(t.getDayOfMonth) +
      Hash.fieldNameEncode(t.getHourOfDay)
    }

    // We want to show the last hour, last day, last month, but that will always span
    // two buckets except for one (minute, hour, day) of the (hour, day, month)
    val bucketHistogramIds = (
      // Last 2 months
      Set(now.minusMonths(1), now).map(monthFmt _)
        .flatMap(month => ids.map("%s:%s".format(month, _))) ++

      // Last 2 days
      Set(now.minusDays(1), now).map(dayFmt _)
        .flatMap(month => ids.map("%s:%s".format(month, _))) ++

      // Last 2 hours
      Set(now.minusHours(1), now).map(hourFmt _)
        .flatMap(month => ids.map("%s:%s".format(month, _))))


    val histograms = BucketRecordHistogram.where(_._id in bucketHistogramIds).fetch
    val notices = NoticeRecord.where(_._id in noticeIds).fetch
    notices.sortBy(_.id).reverse.map(n => {
      val nbSet = n.buckets.value.toSet
      val noticeBuckets = buckets.filter(b => nbSet(b.id))
      val noticeBucketHistograms = histograms.filter(h => nbSet(h.bucket))
      MongoOutgoing(n).addBuckets(noticeBuckets, noticeBucketHistograms, now)
    })
  }

  def get(name: String, key: String, now: DateTime) = {
    get(List(BucketId(name, key).toString), None, now)
  }

  def recentKeys(name: String, limit: Option[Int]): List[String] = {
    BucketRecord.where(_._id startsWith name + ":")
      .select(_._id)
      .orderDesc(_.lastSeen)
      .limitOpt(limit)
      .fetch
  }



  def save(incomingId: ObjectId, incoming: Incoming, bucket: BucketId, maxRecent: Int): SaveResult = {
    val n = incoming.n.getOrElse(1)

    val dateTime = new DateTime(incomingId.getTime)
    val month = Hash.fieldNameEncode(dateTime.getMonthOfYear)
    val day = Hash.fieldNameEncode(dateTime.getDayOfMonth)
    val hour = Hash.fieldNameEncode(dateTime.getHourOfDay)
    val minute = Hash.fieldNameEncode(dateTime.getMinuteOfHour)
    val bucketKey = bucket.toString

    val existing = Stats.time("bucketActions.save.updateBucket") {
      BucketRecord.where(_._id eqs bucketKey)
        .findAndModify(_.noticeCount inc n)
        .and(_.lastSeen setTo incomingId.getTime)
        .and(_.lastVersion setTo incoming.v)
        .and(_.notices push incomingId).upsertOne()
    }

    if (!existing.isDefined) {
      Stats.time("bucketActions.save.upsertBucket") {
        BucketRecord.where(_._id eqs bucketKey)
         .modify(_.firstSeen setTo incomingId.getTime)
         .modify(_.firstVersion setTo incoming.v).upsertOne()
      }
    }

    val noticesToRemove = existing.toList.flatMap(e => {
      val len = e.notices.value.length
      if (len >= maxRecent + maxRecent / 2) {
        logger.info("trimming %d from %s".format(len - maxRecent, bucketKey))
        val toRemove = e.notices.value.take(len - maxRecent)
        Stats.time("bucketActions.save.removeExpiredNotices") {
          BucketRecord.where(_._id eqs bucketKey).modify(_.notices pullAll toRemove).updateOne()
        }
        toRemove
      } else {
        Nil
      }
    })

    val bucketHour = "%s%s%s:%s".format(month, day, hour, bucketKey)
    val bucketDay = "%s%s:%s".format(month, day, bucketKey)
    val bucketMonth = "%s:%s".format(month, bucketKey)
    Stats.time("bucketActions.save.updateHistogram") {
      BucketRecordHistogram.where(_._id eqs bucketHour).modify(_.histogram at minute inc n).upsertOne()
      BucketRecordHistogram.where(_._id eqs bucketDay).modify(_.histogram at hour inc n).upsertOne()
      BucketRecordHistogram.where(_._id eqs bucketMonth).modify(_.histogram at day inc n).upsertOne()
    }

    SaveResult(bucket, existing.map(b => BucketId(b.id)), noticesToRemove)
  }

  def deleteOldHistograms(time: Long, doIt: Boolean = true) {
    val dateTime = new DateTime(time)
    val oldMonth = dateTime.minusMonths(2)
    val oldDay = dateTime.minusDays(2)
    val oldHour = dateTime.minusHours(2)

    val oldMonthField = Hash.fieldNameEncode(oldMonth.getMonthOfYear)

    val oldDaysPattern = Pattern.compile("^%s[%s]".format(
      Hash.fieldNameEncode(oldDay.getMonthOfYear),
      (0 to oldDay.getDayOfMonth).map(Hash.fieldNameEncode _).mkString("")))

    val oldHoursPattern = Pattern.compile("^%s%s[%s]".format(
      Hash.fieldNameEncode(oldHour.getMonthOfYear),
      Hash.fieldNameEncode(oldHour.getDayOfMonth),
      (0 to oldHour.getHourOfDay).map(Hash.fieldNameEncode _).mkString("")))

    val deleteQueries = List(
      BucketRecordHistogram.where(_._id startsWith oldMonthField),
      BucketRecordHistogram.where(_._id matches oldDaysPattern),
      BucketRecordHistogram.where(_._id matches oldHoursPattern))

    deleteQueries.foreach(q => {
      logger.info("deleting: %s".format(q))
      if (doIt) {
        Stats.time("bucketActions.save.deleteOldHistograms.delete") {
          q.bulkDelete_!!!()
        }
      }
    })
  }
}
