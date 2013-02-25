// Copyright 2013 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.BackgroundAction
import com.foursquare.exceptionator.model.io.{BacktraceLine, BucketId, Incoming}
import com.foursquare.exceptionator.filter.ProcessedIncoming
import com.foursquare.exceptionator.filter.concrete.FreshBucketFilter
import com.foursquare.exceptionator.util.{ConcreteBlamer, ConcreteMailSender, Config, IncomingFilter}
import com.twitter.util.Future
import scalaj.collection.Imports._

class EmailFreshExceptionBackgroundAction extends BackgroundAction {
  val mailSender = new ConcreteMailSender
  val blamer = new ConcreteBlamer
  val hostname = java.net.InetAddress.getLocalHost.getHostName.toString

  def postSave(processedIncoming: ProcessedIncoming): Future[Unit] = {
    sendMail(processedIncoming)
  }

  def sendMail(processedIncoming: ProcessedIncoming): Future[Unit] = {
    val incoming = processedIncoming.incoming
    val buckets = processedIncoming.buckets
    if (buckets.exists(_.name == FreshBucketFilter.name)) {
      val interesting = incoming.firstNInteresting(Config.opt(_.getInt("email.nInteresting")).getOrElse(1))
      if (!incoming.sess.get("_email").exists(_ == "false")) {
        val rev = incoming.env.get("git").filter(_ != "0").getOrElse(incoming.v)
        val blameInfo = blameList(rev, interesting)
        formatAndSendMail(incoming, buckets, blameInfo)
      } else {
        Future.Unit
      }
    } else {
      Future.Unit
    }
  }

  def blameList(rev: String, interesting: List[BacktraceLine]): Future[List[String]] = {
    val blames: List[Future[Option[String]]] = interesting.map(bl => {
      blamer.blame(
        rev,
        bl.fileName,
        bl.number,
        bl.method.split(".").toList.toSet).map( _.filter(_.isValid).map(blame =>
          "(%s:%d)\n[%s on %s]\n%s\n".format(
            blame.filePath,
            blame.lineNum,
            blame.author,
            blame.date,
            blame.lineString))
      )
    })
    Future.collect(blames).map(_.flatten.toList)
  }

  def formatAndSendMail(incoming: Incoming, buckets: Set[BucketId], interestingInfo: Future[List[String]]): Future[Unit] = {
    // Find the config whose filter matches
    val routeConfig = Config.opt(_.getConfigList("email.routes").asScala)
    val route = routeConfig.map(IncomingFilter.checkFilters(incoming, _)).flatten.toList.map(_.matchedConfig)

    // Extract the to and cc string list fields from the matching config if available
    val additionalTo = route.map(Config.opt(_, _.getStringList("to").asScala.toList)).toList.flatten.flatten
    val additionalCc = route.map(Config.opt(_, _.getStringList("cc").asScala.toList)).toList.flatten.flatten

    val subject = incoming.msgs.head.substring(0, math.min(incoming.msgs.head.length, 50))
    interestingInfo.flatMap(info =>
      mailSender.send(additionalTo, additionalCc, "[exceptionator] " + subject,
"""New Exception:
%s

%s

Host:
%s

Potentially interesting line(s):
%s

Full trace:
%s

Truly yours,
Exceptionator""".format(
        buckets.find(_.name == "s").map(sb => "http://%s/notices/s/%s".format(
          Config.opt(_.getString("email.prettylinkhost"))
            .getOrElse("%s:%d".format(hostname, Config.opt(_.getInt("http.port")).getOrElse(8080))),
          sb.key)).getOrElse("<unknown>"),
        incoming.msgs.head,
        incoming.h,
        info.mkString("\n"),
        incoming.flatBacktrace.mkString("\n")))
    )
  }
}
