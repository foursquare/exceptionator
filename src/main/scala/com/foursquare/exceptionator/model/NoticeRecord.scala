// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.Incoming
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}
import net.liftweb.mongodb.record.field.{BsonRecordListField, MongoCaseClassField, MongoListField}
import net.liftweb.record.field._
import net.liftweb.json._
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}
import com.foursquare.rogue._
import com.foursquare.rogue.index.{Asc, IndexedRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date


class NoticeRecord extends MongoRecord[NoticeRecord] with MongoId[NoticeRecord] {
  def meta = NoticeRecord

  def createTime = new DateTime(id.getTime(), DateTimeZone.UTC).toDate

  object notice extends MongoCaseClassField[NoticeRecord, Incoming](this) {
    override def name = "n"
  }

  object tags extends MongoListField[NoticeRecord, String](this) {
    override def name = "t"
  }

  object keywords extends MongoListField[NoticeRecord, String](this) {
    override def name = "kw"
  }

  object buckets extends MongoListField[NoticeRecord, String](this) {
    override def name = "b"
  }
}

object NoticeRecord extends NoticeRecord with MongoMetaRecord[NoticeRecord] with IndexedRecord[NoticeRecord] {
  override def collectionName = "notices"

  override val mongoIndexList = List(
    NoticeRecord.index(_._id, Asc),
    NoticeRecord.index(_.keywords, Asc))

  def createRecordFrom(incoming: Incoming): NoticeRecord = {
    val rec = createRecord.notice(incoming)
    incoming.d.foreach(epoch => rec._id(new ObjectId(new Date(epoch))))
    rec
  } 
}
