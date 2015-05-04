// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.Incoming
import com.foursquare.index.{Asc, IndexedRecord}
import com.foursquare.rogue._
import com.foursquare.rogue.lift.LiftRogue._
import java.util.Date
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, MongoId}
import net.liftweb.mongodb.record.field.{MongoListField, MongoMapField, ObjectIdField, DateField}
import net.liftweb.record.field._
import org.bson.types.ObjectId


/** A stripped down notice to be stored for history lookup. */
class HistoryRecord extends MongoRecord[HistoryRecord] with MongoId[HistoryRecord] {
  def meta = HistoryRecord

  object exception extends StringField[HistoryRecord](this, 32) {
    override def name = "e"
  }

  object buckets extends MongoListField[HistoryRecord, String](this) {
    override def name = "b"
  }

  object environment extends MongoMapField[HistoryRecord, String](this) {
    override def name = "en"
  }

  object host extends StringField[HistoryRecord](this, 50) {
    override def name = "h"
  }

  object version extends StringField[HistoryRecord](this, 50) {
    override def name = "v"
  }

  object counted extends IntField[HistoryRecord](this) {
    override def name = "c"
    override def optional_? = true
  }

  object date extends DateField[HistoryRecord](this) {
    override def name = "d"
    override def optional_? = true
  }

}

object HistoryRecord extends HistoryRecord
    with MongoMetaRecord[HistoryRecord]
    with IndexedRecord[HistoryRecord]
{
  override def collectionName = "history"

  override val mongoIndexList = List(
    HistoryRecord.index(_._id, Asc))

  def createRecordFrom(incoming: Incoming): HistoryRecord = {
    val rec = createRecord
    rec.environment(incoming.env)
    rec.host(incoming.h)
    rec.version(incoming.v)
    rec.counted(incoming.n)
    incoming.d.foreach(epoch => {
      rec._id(new ObjectId(new Date(epoch)))
      rec.date(new Date(epoch))
    })
    rec
  }
}
