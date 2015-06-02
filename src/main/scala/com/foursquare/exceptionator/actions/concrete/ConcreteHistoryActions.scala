// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{HistoryActions, IndexActions}
import com.foursquare.exceptionator.model.{HistoryRecord, NoticeRecord}
import com.foursquare.exceptionator.util.{Config, Logger, ReservoirSampler}
import com.foursquare.rogue.lift.LiftRogue._
import com.twitter.ostrich.stats.Stats
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import net.liftweb.json.{JField, JInt, JObject}
import scala.collection.JavaConverters.mapAsScalaConcurrentMapConverter
import scala.util.Random
import com.twitter.conversions.time._
import com.twitter.ostrich.stats.Stats
import com.twitter.util.ScheduledThreadPoolTimer


class ConcreteHistoryActions extends HistoryActions with IndexActions with Logger {
  val flushPeriod = Config.opt(_.getInt("history.flushPeriod")).getOrElse(60)
  val sampleRate = Config.opt(_.getInt("history.sampleRate")).getOrElse(50)
  val samplers = (new ConcurrentHashMap[Date, ReservoirSampler[NoticeRecord]]).asScala
  val timer = new ScheduledThreadPoolTimer(makeDaemons=true)

  timer.schedule(flushPeriod seconds, flushPeriod seconds) {
    try {
      Stats.time("historyActions.flushHistory") {
        flushHistory()
      }
    } catch {
      case t: Throwable => logger.debug("Error flushing history", t)
    }
  }

  def ensureIndexes {
    List(HistoryRecord).foreach(metaRecord => {
      metaRecord.mongoIndexList.foreach(i =>
        metaRecord.ensureIndex(JObject(i.asListMap.map(fld => JField(fld._1, JInt(fld._2.toString.toInt))).toList)))
    })
  }

  // Write history to mongo
  def flushHistory(): Unit = {
    for {
      historyId <- samplers.keys
      sampler <- samplers.remove(historyId)
    } {
      HistoryRecord.where(_.id eqs historyId).get()
        .filter(_.sampleRate.value == sampler.size)
        .map(existing => {
          logger.debug(s"Merging histories for ${historyId}")
          Stats.time("historyActions.flushMerge") {
            val existingState = ReservoirSampler.State(existing.notices.value, existing.totalSampled.value)
            val existingSampler = ReservoirSampler(existing.sampleRate.value, existingState)
            val merged = ReservoirSampler.merge(existingSampler, sampler)
            val state = merged.state
            existing.notices(state.samples.toList).totalSampled(state.sampled).save
          }
        }).getOrElse {
          logger.debug(s"Writing new history for ${historyId}")
          Stats.time("historyActions.flushNew") {
            val state = sampler.state
            HistoryRecord.createRecord
              .id(historyId)
              .notices(state.samples.toList)
              .sampleRate(sampleRate)
              .totalSampled(state.sampled)
              .save
          }
        }
    }
  }

  // Save a notice to its HistoryRecord, using reservoir sampling
  def save(notice: NoticeRecord): Date = {
    val historyId = HistoryRecord.idForTime(notice.createTime)
    Stats.time("historyActions.updateSampler") {
      val sampler = samplers.getOrElseUpdate(historyId, new ReservoirSampler[NoticeRecord](sampleRate))
      sampler.update(notice)
    }
    historyId
  }
}
