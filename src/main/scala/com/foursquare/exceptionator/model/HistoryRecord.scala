// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.Incoming
import net.liftweb.common.{Box, Full}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.mongodb.record.field.{DateField, MongoListField}
import net.liftweb.record.field._
import com.foursquare.rogue._
import com.foursquare.index.{Asc, IndexedRecord}
import com.foursquare.rogue.lift.LiftRogue._
import com.mongodb.{BasicDBList, DBObject}
import java.util.Date


/** Stores one minute of sampled history. Uses the starting timestamp as its _id. */
class HistoryRecord extends MongoRecord[HistoryRecord] {
  def meta = HistoryRecord

  object id extends DateField[HistoryRecord](this) {
    override def name = "_id"
  }

  // TODO(jacob): must subclass this before it will work, see http://liftweb.net/api/26/api/index.html#net.liftweb.mongodb.record.field.MongoListField
  object notices extends MongoListField[HistoryRecord, NoticeRecord](this) {
    override def name = "n"

    override def asDBObject: DBObject = this.synchronized {
      val dbl = new BasicDBList
      value.foreach(v => dbl.add(v.asDBObject))
      dbl
    }

    override def setFromDBObject(dbo: DBObject): Box[MyType] = {
      setBox(Full(dbo match {
        case list: BasicDBList => {
          (for (i <- 0 to list.size-1) yield (NoticeRecord.fromDBObject(list.get(i).asInstanceOf[DBObject]))).toList
        }
        case _ => Nil
      }))
    }
  }

  object sampleRate extends IntField(this) {
    override def name = "r"
  }

  object totalSampled extends IntField(this) {
    override def name = "s"
  }
}

object HistoryRecord extends HistoryRecord with MongoMetaRecord[HistoryRecord] with IndexedRecord[HistoryRecord] {
  override def collectionName = "history"

  // Round down a date to the nearest minute
  def idForTime(date: Date): Date = new Date((date.getTime / 60000) * 60000)

  override val mongoIndexList = List(
    HistoryRecord.index(_.id, Asc))
}
