// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{IndexActions, NoticeActions}
import com.foursquare.exceptionator.model.io.{BucketId, Incoming, Outgoing}
import com.foursquare.exceptionator.model.{NoticeRecord, MongoOutgoing}
import net.liftweb.json._
import org.bson.types.ObjectId
import scalaj.collection.Imports._
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.ostrich.stats.Stats

import com.foursquare.exceptionator.util.{Config, Logger}

class ConcreteNoticeActions extends NoticeActions with IndexActions with Logger {
  def get(ids: List[ObjectId]): List[Outgoing] = {
    NoticeRecord.where(_.id in ids).fetch.map(MongoOutgoing(_))
  }

  def search(keywords: List[String], limit: Option[Int]) = {
    NoticeRecord.where(_.keywords all keywords).orderDesc(_.id).limitOpt(limit).fetch.map(MongoOutgoing(_))
  }

  def ensureIndexes {
    List(NoticeRecord).foreach(metaRecord => {
      metaRecord.mongoIndexList.foreach(i =>
        metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  def save(incoming: Incoming, tags: Set[String], keywords: Set[String], buckets: Set[BucketId]): NoticeRecord = {
    NoticeRecord.createRecordFrom(incoming)
      .keywords(keywords.toList)
      .tags(tags.toList)
      .buckets(buckets.toList.map(_.toString))
      .save
  }

  def addBucket(id: ObjectId, bucketId: BucketId) {
    Stats.time("incomingActions.addBucket") {
      NoticeRecord.where(_.id eqs id).modify(_.buckets push bucketId.toString).updateOne()
    }
  }

  def removeBucket(id: ObjectId, bucketId: BucketId) {
    val result = Stats.time("noticeActions.removeBucket") {
      NoticeRecord.where(_.id eqs id)
      .select(_.buckets)
      .findAndModify(_.buckets pull bucketId.toString).updateOne(true)
    }

    if (result.exists(_.isEmpty)) {
      logger.debug("deleting " + id.toString)
      Stats.time("noticeActions.removeBucket.deleteRecord") {
        NoticeRecord.delete("_id", id)
      }
    }
  }
}
