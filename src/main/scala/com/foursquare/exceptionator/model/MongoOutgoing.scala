// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.
package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.{BucketId, Outgoing}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.bson.types.ObjectId
import org.joda.time.DateTime

object MongoOutgoing {
  def apply(nr: NoticeRecord): MongoOutgoing = {
    val ast = nr.asJValue
    val merged = {
      (("id" -> nr.id.toString) ~
      ("d" -> nr.id.getTime) ~
      ("kw" -> nr.keywords.value) ~
      ("tags" -> nr.tags.value) ~
      ("bkts" -> nr.buckets.value.map(id => {
        val bId = BucketId(id)
        bId.name -> Map("nm" -> bId.name, "k" -> bId.key)
      }).toMap)) merge (ast \ nr.notice.name)
    }
    MongoOutgoing(nr.id, merged)
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
}
