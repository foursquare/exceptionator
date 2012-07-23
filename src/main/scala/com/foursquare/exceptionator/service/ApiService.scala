// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.service

import com.codahale.jerkson.Json.generate
import com.foursquare.exceptionator.model.io.{Incoming, Outgoing}
import com.foursquare.exceptionator.actions.{BucketActions, NoticeActions}
import com.twitter.finagle.http.{Response, Request}
import scalaj.collection.Imports._
import org.jboss.netty.buffer.{ChannelBufferInputStream, ChannelBuffers}
import org.jboss.netty.handler.codec.http._
import com.twitter.finagle.Service
import com.twitter.ostrich.stats.Stats
import com.foursquare.exceptionator.util.{Config, Logger}
import com.twitter.util.{Future, FuturePool, Throw}
import java.util.concurrent.Executors
import org.joda.time.DateTime

object ApiHttpService {
   val Notices = """/api/notices(?:/(\w+)(?:/([^/?&=]+))?)?""".r
}
class ApiHttpService(
  noticeActions: NoticeActions,
  bucketActions: BucketActions,
  bucketFriendlyNames: Map[String, String]) extends Service[Request, Response] with Logger {

  case class InternalResponse(content: Future[String], status: HttpResponseStatus = HttpResponseStatus.OK)

  val apiFuturePool = FuturePool(Executors.newFixedThreadPool(10))

  def apply(request: Request) = {

    request.method match {
      case HttpMethod.GET =>
        val res: InternalResponse = request.path match {
          case "/api/config" =>
            config
          case ApiHttpService.Notices(name, key) =>
            notices(Option(name), Option(key), request)
          case "/api/search" =>
            search(request.getParam("q").toLowerCase, request)
          case _ => 
            InternalResponse(Future.value(""), HttpResponseStatus.NOT_FOUND)
        }
        res.content.map(content => {
          val response = Response(HttpVersion.HTTP_1_1, res.status)
          response.contentString = content
          response.setContentTypeJson
          response
        })
      case _ =>
        ServiceUtil.errorResponse(HttpResponseStatus.NOT_FOUND)
    }
  }

  def limitParam(request: Request) = request.getIntParam("limit", 20)

  def bucketNotices(bucketName: String, bucketKey: String, request: Request) = {
    InternalResponse(apiFuturePool({
      val outgoingElems = bucketActions.get(bucketName, bucketKey, DateTime.now)
      val outgoing = Outgoing.compact(outgoingElems.take(limitParam(request)))
      outgoing
    }))
  }

  def recent(bucketName: String, request: Request) = {
    InternalResponse(apiFuturePool({
      val limit = limitParam(request)
      val outgoingElems = bucketActions.get(bucketActions.recentKeys(bucketName, Some(limit)), Some(1), DateTime.now)
      val outgoing = Outgoing.compact(outgoingElems.take(limit))
      outgoing
    }))
  }

  def notices(
    bucketName: Option[String],
    bucketId: Option[String],
    request: Request) = {

    (bucketName, bucketId) match {
      case (None, None) => recent("s", request)
      case (Some(n), None) => recent(n, request)
      case (Some(n), Some(k)) => bucketNotices(n, k, request)
    }
  }

  def search(terms: String, request: Request) = {
    InternalResponse(apiFuturePool({
      val limit = limitParam(request)
      val outgoingElems = noticeActions.search(terms.split("\\s+").toList, Some(limit))
      val outgoing = Outgoing.compact(outgoingElems.take(limit))
      outgoing
    }))
  }

  def config = {
    val values = Map(
      "friendlyNames" -> bucketFriendlyNames) ++
      Config.opt(_.getInt("http.port")).map("apiPort" -> _).toMap ++
      Config.opt(_.getString("http.hostname")).map("apiHost" -> _).toMap
    InternalResponse(Future.value(generate(values)))
  }
}
