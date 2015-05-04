// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{IndexActions, HistoryActions}
import com.foursquare.exceptionator.model.HistoryRecord
import com.foursquare.exceptionator.model.io.{BucketId, Incoming}
import com.foursquare.exceptionator.util.Logger
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.ostrich.stats.Stats
import net.liftweb.json._
import org.bson.types.ObjectId


class ConcreteHistoryActions extends HistoryActions with IndexActions with Logger {
  // def get(ids: List[ObjectId]): List[Outgoing] = {
  //   NoticeRecord.where(_._id in ids).fetch.map(MongoOutgoing(_))
  // }

  def ensureIndexes {
    List(HistoryRecord).foreach(metaRecord => {
      metaRecord.mongoIndexList.foreach(i =>
        metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  def save(exceptionId: String, incoming: Incoming, buckets: Set[BucketId]): ObjectId = {
    val hr = HistoryRecord.createRecordFrom(incoming)
      .exception(exceptionId)
      .buckets(buckets.toList.map(_.toString))
      .save
    hr._id.value
  }

  def addBucket(id: ObjectId, bucketId: BucketId) {
    Stats.time("incomingActions.addHistoryBucket") {
      HistoryRecord.where(_._id eqs id).modify(_.buckets push bucketId.toString).updateOne()
    }
  }

  def removeBucket(id: ObjectId, bucketId: BucketId) {
    val result = Stats.time("historyActions.removeBucket") {
      HistoryRecord.where(_._id eqs id)
      .select(_.buckets)
      .findAndModify(_.buckets pull bucketId.toString).updateOne(true)
    }

    if (result.exists(_.isEmpty)) {
      logger.debug("deleting " + id.toString)
      Stats.time("historyActions.removeBucket.deleteRecord") {
        HistoryRecord.delete("_id", id)
      }
    }
  }
}
