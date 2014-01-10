// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.util

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import com.twitter.util.{ScheduledThreadPoolTimer, TimerTask}
import com.twitter.conversions.time._

object PollingCache {
  val timer = new ScheduledThreadPoolTimer(2, "PollingCache", true)
}

class PollingCache[T](fetch: () => T, frequency: Int) extends Logger {
  @volatile protected var cache: T = fetch()
  PollingCache.timer.schedule(0.millis, frequency.seconds) {
    try {
      cache = fetch()
    } catch {
      case e: Exception => logger.error(e, "Error fetching cache")
    }
  }
  def get: T = cache
}
