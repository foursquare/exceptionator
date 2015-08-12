// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.service

import com.codahale.jerkson.Json.{parse, stream}
import com.foursquare.exceptionator.actions.IndexActions
import com.foursquare.exceptionator.actions.{HasBucketActions, HasHistoryActions, HasNoticeActions,
    HasUserFilterActions}
import com.foursquare.exceptionator.loader.service.HasPluginLoaderService
import com.foursquare.exceptionator.loader.concrete.ConcretePluginLoaderService
import com.foursquare.exceptionator.actions.concrete.{ConcreteBackgroundActions, ConcreteBucketActions,
    ConcreteHistoryActions, ConcreteIncomingActions, ConcreteNoticeActions, ConcreteUserFilterActions,
    FilteredConcreteIncomingActions}
import com.foursquare.exceptionator.util.{Config, Logger, ConcreteMailSender, ConcreteBlamer}
import com.mongodb.{MongoClient, DBAddress, MongoException, MongoClientOptions, ServerAddress}
import com.twitter.finagle.builder.{ServerBuilder, Server}
import com.twitter.finagle.http.{RichHttp, Http, Response, Request}
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.Service
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.ostrich.admin.{AdminServiceFactory, RuntimeEnvironment, StatsFactory, TimeSeriesCollectorFactory}
import com.twitter.ostrich.stats.Stats
import com.twitter.util.{Future, FuturePool, Throw}
import java.io.{InputStream, IOException}
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



class StaticFileService(prefix: String) extends Service[ExceptionatorRequest, Response] with Logger {

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


  def apply(request: ExceptionatorRequest) = {
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
          response.headerMap.add(HttpHeaders.Names.CONTENT_TYPE, "application/x-javascript")
        }
        if (path.endsWith(".css")) {
          response.headerMap.add(HttpHeaders.Names.CONTENT_TYPE, "text/css")
        }
        response
    })).getOrElse(ServiceUtil.errorResponse(HttpResponseStatus.NOT_FOUND))
  }
}

class ExceptionatorHttpService(
    fileService: Service[ExceptionatorRequest, Response],
    apiService: Service[ExceptionatorRequest, Response],
    incomingService: Service[ExceptionatorRequest, Response]) extends Service[ExceptionatorRequest, Response] {

  def apply(request: ExceptionatorRequest) = {
    if (!request.path.startsWith("/api/")) {
      fileService(request)
    } else {
      // TODO(johng) why did i make this hard on myself?
      if (request.method == HttpMethod.POST &&
          ( request.path.startsWith("/api/notice") ||
            request.path.startsWith("/api/multi-notice"))) {
        incomingService(request)
      } else {
        apiService(request)
      }
    }
  }
}


object ExceptionatorServer extends Logger {
  val defaultPort = 8080
  val defaultStatsPort = defaultPort + 1
  val defaultDbHost = "localhost:27017"
  val defaultDbName = "test"

  def bootMongo(indexesToEnsure: List[IndexActions] = Nil) {
    // Mongo
    val dbServerConfig = Config.opt(_.getString("db.host")).getOrElse(defaultDbHost)
    val dbServers = dbServerConfig.split(",").toList.map(a => a.split(":") match {
      case Array(h,p) => new ServerAddress(h, p.toInt)
      case _ => throw new Exception("didn't understand host " + a)
    })
    val mongoOptions = MongoClientOptions.builder
      .socketTimeout(10 * 1000)
      .build
    try {
      val mongo = new MongoClient(dbServers.asJava, mongoOptions)
      val dbname = Config.opt(_.getString("db.name")).getOrElse(defaultDbName)
      MongoDB.defineDb(DefaultMongoIdentifier, mongo, dbname)
      indexesToEnsure.foreach(_.ensureIndexes)
    } catch {
      case e: MongoException =>
        logger.error(e, "Failed ensure indexes on %s because: %s.  Is mongo running?"
          .format(dbServerConfig, e.getMessage))
        throw e
    }
  }


  def main(args: Array[String]) {
    logger.info("Starting ExceptionatorServer")
    Config.defaultInit()

    val services = new HasBucketActions
        with HasHistoryActions
        with HasNoticeActions
        with HasPluginLoaderService
        with HasUserFilterActions {
      lazy val bucketActions = new ConcreteBucketActions
      lazy val historyActions = new ConcreteHistoryActions(this)
      lazy val noticeActions = new ConcreteNoticeActions
      lazy val pluginLoader = new ConcretePluginLoaderService(this)
      lazy val userFilterActions = new ConcreteUserFilterActions
    }

    // Create services
    val incomingActions = new FilteredConcreteIncomingActions(
      new ConcreteIncomingActions(services))

    // Start mongo
    try {
      bootMongo(List(
        services.bucketActions,
        services.historyActions,
        services.noticeActions,
        services.userFilterActions))
    } catch {
      case e: IOException => {
        logger.error(e, "Failed to connect to mongo")
        System.exit(1)
      }
    }

    val backgroundActions = new ConcreteBackgroundActions(services)

    // Start ostrich
    val runtime = RuntimeEnvironment(this, Array[String]())

    AdminServiceFactory(
      httpPort = (Config.opt(_.getInt("stats.port")).getOrElse(defaultStatsPort)))
      .addStatsFactory(StatsFactory(reporters = List(TimeSeriesCollectorFactory())))
      .apply(runtime)

    val httpPort = Config.opt(_.getInt("http.port")).getOrElse(defaultPort)
    logger.info("Starting ExceptionatorHttpService on port %d".format(httpPort))


    // Start Http Service
    val service = ExceptionFilter andThen new DefaultRequestEnricher andThen
      new ExceptionatorHttpService(
        new StaticFileService(""),
        new ApiHttpService(services, incomingActions.bucketFriendlyNames),
        new IncomingHttpService(incomingActions, backgroundActions))

    val server: Server = ServerBuilder()
        .bindTo(new InetSocketAddress(httpPort))
        .codec(new RichHttp[Request](Http.get))
        .name("exceptionator-http")
        .reportTo(new OstrichStatsReceiver)
        .build(service)
  }
}
