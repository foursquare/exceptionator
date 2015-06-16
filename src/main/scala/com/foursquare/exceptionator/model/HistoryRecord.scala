// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.Incoming
import com.foursquare.exceptionator.util.Config
import com.foursquare.index.{Asc, IndexedRecord}
import com.foursquare.rogue._
import com.foursquare.rogue.lift.LiftRogue._
import com.mongodb.{BasicDBList, DBObject}
import com.twitter.ostrich.stats.Stats
import java.util.concurrent.ConcurrentHashMap
import net.liftweb.common.{Box, Full}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.mongodb.record.field.{BsonRecordListField, DateField}
import net.liftweb.record.field._
import org.joda.time.DateTime
import scala.collection.JavaConverters.mapAsScalaConcurrentMapConverter


/** Stores one minute of sampled history. Uses the starting timestamp as its _id. */
class HistoryRecord extends MongoRecord[HistoryRecord] {
  def meta = HistoryRecord

  object id extends DateField[HistoryRecord](this) {
    override def name = "_id"
    def apply(in: DateTime): HistoryRecord = apply(in.toDate)
    def dateTimeValue: DateTime = new DateTime(value)
  }

  object notices extends BsonRecordListField[HistoryRecord, NoticeRecord](this, NoticeRecord) {
    override def name = "n"
  }

  object sampleRate extends IntField(this) {
    override def name = "r"
  }

  object window extends IntField(this) {
    override def name = "w"
    override def defaultValue = HistoryRecord.windowSecs
  }

  object totalSampled extends IntField(this) {
    override def name = "s"
  }
}

object HistoryRecord extends HistoryRecord with MongoMetaRecord[HistoryRecord] with IndexedRecord[HistoryRecord] {
  override def collectionName = "history"

  val windowSecs = Config.opt(_.getInt("history.sampleWindowSeconds")).getOrElse(60)
  val windowMillis = windowSecs * 1000L

  // cache data for history histograms so we don't do expensive mongo calls on every request
  // Note: this will go away once the histogram system is overhauled to support history itself
  lazy val histogramData = Stats.time("history.histograms.loadFromDb") {
    val empty = (new ConcurrentHashMap[Long, Int]).asScala
    val allPairs = HistoryRecord
      .select(_.id, _.totalSampled)
      .fetchBatch(1000)(_.map({ case (d, v) => d.getTime -> v }))
    empty ++= allPairs
    empty
  }

  // round <base> down to 0 mod <mod>
  def roundMod(base: Long, mod: Long): Long = (base/mod) * mod
  def idForTime(date: DateTime): DateTime = new DateTime(roundMod(date.getMillis, windowMillis))

  override val mongoIndexList = List(
    HistoryRecord.index(_.id, Asc))
}
