package com.foursquare.exceptionator.util
import com.twitter.logging.Level.INFO
import com.twitter.logging.config._

object Logger {
  lazy val config = (new LoggerConfig {
    node = ""
    level = INFO
    handlers = List(new ConsoleHandlerConfig)
  }).apply()
}

trait Logger {
  val _ = Logger.config
  val logger = com.twitter.logging.Logger.get(getClass)
}
