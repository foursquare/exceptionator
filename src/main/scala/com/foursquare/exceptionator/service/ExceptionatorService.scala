// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.service

import com.codahale.jerkson.Json.{parse, stream}
import com.foursquare.exceptionator.actions.IndexActions
import com.foursquare.exceptionator.actions.concrete.{ConcreteBackgroundActions, ConcreteBucketActions,
  ConcreteIncomingActions, ConcreteNoticeActions, FilteredConcreteIncomingActions}
import com.foursquare.exceptionator.util.{Config, Logger, ConcreteMailSender, ConcreteBlamer}
import com.mongodb.{Mongo, DBAddress, MongoException, ServerAddress}
import com.twitter.finagle.builder.{ServerBuilder, Server}
import com.twitter.finagle.http.{RichHttp, Http, Response, Request}
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.Service
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.ostrich.admin.{AdminServiceFactory, RuntimeEnvironment, StatsFactory, TimeSeriesCollectorFactory}
import com.twitter.ostrich.stats.Stats
import com.twitter.util.{Future, FuturePool, Throw}
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import org.jboss.netty.buffer.{ChannelBufferInputStream, ChannelBuffers}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil
import scala.collection.mutable.ListBuffer
import scalaj.collection.Imports._

object ServiceUtil {
  def errorResponse(status: HttpResponseStatus) = {
    val response = Response(HttpVersion.HTTP_1_1, status)
    Future.value(response)
  }
}


class StaticFileService(prefix: String) extends Service[Request, Response] with Logger {

  val staticFileFuturePool = FuturePool(Executors.newFixedThreadPool(8))

  def inputStreamToByteArray(is: InputStream): Array[Byte] = {
    val buf = ListBuffer[Byte]()
    var b = is.read()
    while (b != -1) {
        buf.append(b.byteValue)
        b = is.read()
    }
    buf.toArray
  }


  def apply(request: Request) = {
    val path = if (request.path.startsWith("/content")) {
      request.path
    } else {
      "/content/index.html"
    }
    val resourcePath = prefix + path
    logger.info("GET %s from %s".format(path, resourcePath))
    val stream = Option(getClass.getResourceAsStream(resourcePath))

    stream.map(s =>  staticFileFuturePool(inputStreamToByteArray(s)).map(data => {
      val response = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        response.setContent(ChannelBuffers.copiedBuffer(data))
        if (path.endsWith(".js")) {
           response.setHeader("Content-Type", "application/x-javascript")
        }
        if (path.endsWith(".css")) {
           response.setHeader("Content-Type", "text/css")
        }
        response
    })).getOrElse(ServiceUtil.errorResponse(HttpResponseStatus.NOT_FOUND))
  }
}

class ExceptionatorHttpService(
    fileService: StaticFileService,
    apiService: ApiHttpService,
    incomingService: IncomingHttpService) extends Service[Request, Response] {

  def apply(request: Request) = {
    // This is how you parse request parameters
    val queryString = new QueryStringDecoder(request.getUri())
    val params = queryString.getParameters().asScala
    val path = queryString.getPath()

    if (!path.startsWith("/api/")) {
      fileService(request)
    } else {
      request.method match {
        case HttpMethod.POST =>
          incomingService(request)
        case HttpMethod.GET =>
          apiService(request)
        case _ =>
          ServiceUtil.errorResponse(HttpResponseStatus.METHOD_NOT_ALLOWED)
      }
    }
  }
}


object ExceptionatorServer extends Logger {
  val defaultPort = 8080

  def bootMongo(indexesToEnsure: List[IndexActions] = Nil) {
    // Mongo
    val dbServerConfig = Config.opt(_.getString("db.host")).getOrElse("127.0.0.1:27017")
    val dbServers = dbServerConfig.split(",").toList.map(a => a.split(":") match {
      case Array(h,p) => new ServerAddress(h, p.toInt)
      case _ => throw new Exception("didn't understand host " + a)
    })
    val mongo = new Mongo(dbServers.asJava)
    val dbname = Config.opt(_.getString("db.name")).getOrElse("test")

    logger.info("Using mongo: %s".format(dbServerConfig)) 
    logger.info("Using database: %s".format(dbname))
    MongoDB.defineDb(DefaultMongoIdentifier, mongo, dbname)

    try {
      indexesToEnsure.foreach(_.ensureIndexes)
    } catch {
      case e: MongoException =>
        logger.error("Failed ensure indexes on %s because: %s.  Is mongo running?"
          .format(dbServerConfig, e.getMessage), e)
        throw e;
    }
  }
  

  def main(args: Array[String]) {
    logger.info("Starting ExceptionatorServer")
    Config.defaultInit()

    // Create services
    val backgroundActions = new ConcreteBackgroundActions(new ConcreteMailSender, new ConcreteBlamer)
    val bucketActions = new ConcreteBucketActions
    val noticeActions = new ConcreteNoticeActions
    val incomingActions = new FilteredConcreteIncomingActions(
      new ConcreteIncomingActions(noticeActions, bucketActions))

      
    // Start mongo
    bootMongo(List(noticeActions, bucketActions))

    // Start ostrich
    val runtime = RuntimeEnvironment(this, Array[String]())

    AdminServiceFactory(
      httpPort = (Config.opt(_.getInt("stats.port")).getOrElse(defaultPort + 1)))
      .addStatsFactory(StatsFactory(reporters = List(TimeSeriesCollectorFactory())))
      .apply(runtime)

    val httpPort = Config.opt(_.getInt("http.port")).getOrElse(defaultPort)
    logger.info("Starting ExceptionatorHttpService on port %d".format(httpPort))


    // Start Http Service
    val service = ExceptionFilter andThen
      new ExceptionatorHttpService(
        new StaticFileService(""),
        new ApiHttpService(noticeActions, bucketActions, incomingActions.bucketFriendlyNames),
        new IncomingHttpService(incomingActions, backgroundActions))

    val server: Server = ServerBuilder()
        .bindTo(new InetSocketAddress(httpPort))
        .codec(new RichHttp[Request](Http.get))
        .name("exceptionator-http")
        .reportTo(new OstrichStatsReceiver)
        .build(service)
  }
}
