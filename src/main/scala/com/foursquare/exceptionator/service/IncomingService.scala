// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.service

import com.codahale.jerkson.Json.{generate, parse, stream}
import com.foursquare.exceptionator.model.io.Incoming
import com.foursquare.exceptionator.filter.FilteredIncoming
import com.foursquare.exceptionator.actions.{BackgroundActions, IncomingActions}
import com.foursquare.exceptionator.util.{Config, Logger}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.Service
import com.twitter.ostrich.stats.Stats
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBufferInputStream
import org.jboss.netty.handler.codec.http._
import java.io.{BufferedWriter, FileWriter}
import java.util.concurrent.Executors
import scalaj.collection.Imports._

class IncomingHttpService(incomingActions: IncomingActions, backgroundActions: BackgroundActions)
    extends Service[ExceptionatorRequest, Response] with Logger {

  val incomingLog = Config.opt(_.getString("log.incoming")).map(fn => new BufferedWriter(new FileWriter(fn, true)))

  def apply(request: ExceptionatorRequest) = {
    request.method match {
      case HttpMethod.POST =>
        request.path match {
          case "/api/notice" =>
            val incoming = parse[Incoming](new ChannelBufferInputStream(request.getContent))
            process(incoming).map(res => {
              val response = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
              response.contentString = res
              response
            })
          case "/api/multi-notice" =>
            val incomingSeq = stream[Incoming](new ChannelBufferInputStream(request.getContent)).toSeq
            Future.collect(incomingSeq.map(incoming => process(incoming))).map(res => {
              val response = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
              response.contentString = res.mkString(",")
              response
            })
          case _ => 
            ServiceUtil.errorResponse(HttpResponseStatus.NOT_FOUND)
        }

      case _ =>
        ServiceUtil.errorResponse(HttpResponseStatus.NOT_FOUND)
    }
  }

  def process(incoming: Incoming) = { 
    incomingLog.foreach(log => {
      log.write(generate(incoming))
      log.write("\n")
    })

    val n = incoming.n.getOrElse(1)
    Stats.incr("notices", n)
    incomingActions(FilteredIncoming(incoming)).map(processed => {
      Stats.time("backgroundActions.postSave") {
        backgroundActions.postSave(processed)
      }
      processed.id.map(_.toString).getOrElse({
        logger.warning("blocked:\n" + incoming)
        ""
      })
    })
  }
}
