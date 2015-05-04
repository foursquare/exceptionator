// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model

import com.foursquare.exceptionator.model.io.Incoming
import com.foursquare.index.{Asc, IndexedRecord}
import com.foursquare.rogue._
import com.foursquare.rogue.lift.LiftRogue._
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.mongodb.record.field.{DateField, MongoListField, StringPk}
import net.liftweb.record.field._

/** A record storing the highly redundant exception data from NoticeRecord.notice */
class ExceptionRecord extends MongoRecord[ExceptionRecord] with StringPk[ExceptionRecord] {
  def meta = ExceptionRecord

  object messages extends MongoListField[ExceptionRecord, String](this) {
    override def name = "m"
  }

  object exceptions extends MongoListField[ExceptionRecord, String](this) {
    override def name = "e"
  }

  object backtrace extends MongoListField[ExceptionRecord, String](this) {
    override def name = "b"
  }

  object firstSeen extends DateField[ExceptionRecord](this) {
    override def name = "df"
  }

  object lastSeen extends DateField[ExceptionRecord](this) {
    override def name = "dl"
  }
}

object ExceptionRecord extends ExceptionRecord
    with MongoMetaRecord[ExceptionRecord]
    with IndexedRecord[ExceptionRecord]
{
  override def collectionName = "exceptions"

  override val mongoIndexList = List(
    ExceptionRecord.index(_.id, Asc))
}
