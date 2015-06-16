// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.
package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.{BucketId, Outgoing}
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.ostrich.stats.Stats
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.bson.types.ObjectId
import org.joda.time.DateTime


object MongoOutgoing {
  def apply(nr: NoticeRecord): MongoOutgoing = {
    val ast = nr.asJValue
    val merged = {
      (("id" -> nr.id.toString) ~
      ("d" -> nr.id.value.getTime) ~
      ("kw" -> nr.keywords.value) ~
      ("tags" -> nr.tags.value) ~
      ("bkts" -> nr.buckets.value.map(id => {
        val bId = BucketId(id)
        bId.name -> Map("nm" -> bId.name, "k" -> bId.key)
      }).toMap)) merge (ast \ nr.notice.name)
    }
    MongoOutgoing(nr.id.value, merged)
  }
}


case class MongoOutgoing(id: ObjectId, doc: JValue) extends Outgoing {
  def addBuckets(buckets: List[BucketRecord], histograms: List[BucketRecordHistogram], now: DateTime): Outgoing = {
    val bucketInfo: List[JField] = buckets.map(bucket => {
      val id = BucketId(bucket.id)
      val histoMaps = List(HistogramType.Hour, HistogramType.Day, HistogramType.Month).map(t => {
        val matched = histograms.filter(h => bucket.id == h.bucket && t == h.histogramType)
        t -> matched.map(_.toEpochMap(now)).flatten.toMap
      }).toMap
      JField(id.name,
        ("nm" -> id.name) ~
        ("k" -> id.key) ~
        ("df" -> bucket.firstSeen.value) ~
        ("dl" -> bucket.lastSeen.value) ~
        ("vf" -> bucket.firstVersion.value) ~
        ("vl" -> bucket.lastVersion.value) ~
        ("h" ->
          ("h" -> histoMaps.get(HistogramType.Hour))~
          ("d" -> histoMaps.get(HistogramType.Day))~
          ("m" -> histoMaps.get(HistogramType.Month))))
    })
    MongoOutgoing(id, JObject(List(JField("bkts", JObject(bucketInfo)))) merge doc)
  }

  def addHistorygrams(): Outgoing = Stats.time("history.histograms.compute") {
    val time = new DateTime(id.getTime)
    val allData = HistoryRecord.histogramData
      .filterKeys(t => time.minusMonths(1).getMillis <= t && t <= time.getMillis)

    val monthMap = allData
      .groupBy({ case (t, v) => HistoryRecord.roundMod(t, HistogramType.Month.step).toString })
      .mapValues(_.foldLeft(0)({ case (total, (_, v)) => total + v }))
    val dayMap = allData
      .filter({ case (t, v) => time.minusDays(1).getMillis < t })
      .groupBy({ case (t, v) => HistoryRecord.roundMod(t, HistogramType.Day.step).toString })
      .mapValues(_.foldLeft(0)({ case (total, (_, v)) => total + v }))
    val hourMap = allData
      .filter({ case (t, v) => time.minusHours(1).getMillis < t })
      .groupBy({ case (t, v) => HistoryRecord.roundMod(t, HistogramType.Hour.step).toString })
      .mapValues(_.foldLeft(0)({ case (total, (_, v)) => total + v }))

    val updated = JObject(List(JField("hist",
      ("h" -> hourMap) ~
      ("d" -> dayMap) ~
      ("m" -> monthMap)
    ))) merge doc

    MongoOutgoing(id, updated)
  }
}
