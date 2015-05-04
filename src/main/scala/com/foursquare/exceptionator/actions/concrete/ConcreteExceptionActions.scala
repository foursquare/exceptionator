// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{IndexActions, ExceptionActions}
import com.foursquare.exceptionator.model.ExceptionRecord
import com.foursquare.exceptionator.model.io.Incoming
import com.foursquare.exceptionator.util.{Hash, Logger}
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.ostrich.stats.Stats
import net.liftweb.json._
import org.joda.time.DateTime


class ConcreteExceptionActions extends ExceptionActions with IndexActions with Logger {
  def ensureIndexes {
    List(ExceptionRecord).foreach(metaRecord => {
      metaRecord.mongoIndexList.foreach(i =>
        metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  def save(incoming: Incoming): String = {
    val btHash = incoming.btHash.getOrElse (Hash.ofString(incoming.bt.mkString("\n")))
    val time = DateTime.now

    val existing = Stats.time("exceptionActions.save.updateException") {
      ExceptionRecord.where(_.id eqs btHash).findAndModify(_.lastSeen setTo time).upsertOne()
    }

    if (!existing.isDefined) {
      Stats.time("exceptionActions.save.upsertException") {
        ExceptionRecord.where(_.id eqs btHash)
          .modify(_.messages setTo incoming.msgs)
          .modify(_.exceptions setTo incoming.excs)
          .modify(_.backtrace setTo incoming.bt)
          .modify(_.firstSeen setTo time).upsertOne()
      }
    }

    btHash
  }
}
