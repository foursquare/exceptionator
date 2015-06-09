// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{HistoryActions, IndexActions}
import com.foursquare.exceptionator.model.{HistoryRecord, MongoOutgoing, NoticeRecord}
import com.foursquare.exceptionator.model.io.Outgoing
import com.foursquare.exceptionator.util.{Config, Logger, ReservoirSampler}
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.conversions.time._
import com.twitter.ostrich.stats.Stats
import com.twitter.util.ScheduledThreadPoolTimer
import java.util.concurrent.ConcurrentHashMap
import net.liftweb.json.{JField, JInt, JObject}
import org.joda.time.DateTime
import scala.collection.JavaConverters.mapAsScalaConcurrentMapConverter
import scala.util.Random


class ConcreteHistoryActions extends HistoryActions with IndexActions with Logger {
  val flushPeriod = Config.opt(_.getInt("history.flushPeriod")).getOrElse(60)
  val sampleRate = Config.opt(_.getInt("history.sampleRate")).getOrElse(50)
  val samplers = (new ConcurrentHashMap[DateTime, ReservoirSampler[NoticeRecord]]).asScala
  val timer = new ScheduledThreadPoolTimer(makeDaemons=true)

  timer.schedule(flushPeriod seconds, flushPeriod seconds) {
    try {
      Stats.time("historyActions.flushHistory") {
        flushHistory()
      }
    } catch {
      case t: Throwable => logger.debug("Error flushing history", t)
    }
  }

  def ensureIndexes {
    List(HistoryRecord).foreach(metaRecord => {
      metaRecord.mongoIndexList.foreach(i =>
        metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  // Write history to mongo
  def flushHistory(): Unit = {
    for {
      historyId <- samplers.keys
      sampler <- samplers.remove(historyId)
    } {
      HistoryRecord.where(_.id eqs historyId).get()
        .filter(_.sampleRate.value == sampler.size)
        .map(existing => {
          logger.debug(s"Merging histories for ${historyId}")
          Stats.time("historyActions.flushMerge") {
            val existingState = ReservoirSampler.State(existing.notices.value, existing.totalSampled.value)
            val existingSampler = ReservoirSampler(existing.sampleRate.value, existingState)
            val merged = ReservoirSampler.merge(existingSampler, sampler)
            val state = merged.state
            val sorted = state.samples.sortBy(_._id.value).reverse
            existing.notices(sorted.toList).totalSampled(state.sampled).save
          }
        }).getOrElse {
          logger.debug(s"Writing new history for ${historyId}")
          Stats.time("historyActions.flushNew") {
            val state = sampler.state
            val sorted = state.samples.sortBy(_._id.value).reverse
            HistoryRecord.createRecord
              .id(historyId)
              .notices(sorted.toList)
              .sampleRate(sampleRate)
              .totalSampled(state.sampled)
              .save
          }
        }
    }
  }

  def get(time: DateTime, limit: Int): List[Outgoing] = {
    val historyId = HistoryRecord.idForTime(time)
    val historyOpt = HistoryRecord.where(_.id lte historyId).orderDesc(_.id).get()
    historyOpt.map { history =>
      val outgoing = history.notices.value.dropWhile(_.createDateTime.isAfter(time)).take(limit).map(MongoOutgoing(_).addHistorygrams())
      if (outgoing.size < limit) {
        // recurse and look at previous records to flush out our list
        outgoing ++ get(history.id.dateTimeValue.minusMillis(1), limit - outgoing.size)
      } else {
        outgoing
      }
    }.getOrElse(Nil)
  }

  // Save a notice to its HistoryRecord, using reservoir sampling
  def save(notice: NoticeRecord): DateTime = {
    // use the client-provided date if available, otherwise record creation time
    val dateTime = notice.notice.value.d.map(new DateTime(_)).getOrElse(notice.createDateTime)
    val historyId = HistoryRecord.idForTime(dateTime)
    val sampler = samplers.getOrElseUpdate(historyId, new ReservoirSampler[NoticeRecord](sampleRate))
    sampler.update(notice)
    historyId
  }
}
