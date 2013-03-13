// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.util

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import com.twitter.util.{ScheduledThreadPoolTimer, TimerTask}
import com.twitter.conversions.time._

object PollingCache {
  val timer = new ScheduledThreadPoolTimer
}

class PollingCache[T](fetch: () => T, frequency: Int) extends Logger {
  @volatile protected var cache: T = fetch()
  PollingCache.timer.schedule(0.millis, frequency.seconds) {
    try {
      cache = fetch()
    } catch {
      case e => logger.error("Error fetching cache", e)
    }
  }
  def get: T = cache
}
