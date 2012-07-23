// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.BackgroundActions
import com.foursquare.exceptionator.model.io.{BucketId, Incoming}
import com.foursquare.exceptionator.filter.ProcessedIncoming
import com.foursquare.exceptionator.filter.concrete.FreshBucketFilter
import com.foursquare.exceptionator.util.{Blamer, Config, IncomingFilter, MailSender}
import scalaj.collection.Imports._
import scala.actors.Actor
import scala.actors.Actor._

case class BackgroundAction(processedIncoming: ProcessedIncoming, f: (ProcessedIncoming) => Unit)

class ConcreteBackgroundActions(mailSender: MailSender, blamer: Blamer)
    extends BackgroundActions {

  val hostname = java.net.InetAddress.getLocalHost.getHostName.toString

  object backgroundActor extends Actor {
    def act() {
      while (true) {
        receive {
          case BackgroundAction(pi, f) => f(pi)
          case _ =>
        }
      }
    }
  }

  backgroundActor.start

  def postSave(processedIncoming: ProcessedIncoming) {
    backgroundActor ! BackgroundAction(processedIncoming, sendMail _)
  }


  def sendMail(processedIncoming: ProcessedIncoming) {
    val incoming = processedIncoming.incoming
    val buckets = processedIncoming.buckets
    if (buckets.exists(_.name == FreshBucketFilter.name)) {
      val interesting = incoming.firstNInteresting(Config.opt(_.getInt("email.nInteresting")).getOrElse(1))
      if (!incoming.sess.get("_email").exists(_ == "false")) {
        val rev = incoming.env.get("git").filter(_ != "0").getOrElse(incoming.v)
        val blames = interesting.map(bl => blamer.blame(rev, bl.fileName, bl.number)).toList.flatten
        val blameInfo = blames.flatMap(blame =>
          (if (blame.valid_?) {
            Some("(%s:%d)\n[%s on %s]\n%s\n".format(blame.filePath, blame.lineNum, blame.author, blame.date, blame.lineString))
          } else {
            None
        }).toList)
        formatAndSendMail(incoming, buckets, blameInfo)
      }
    }
  }


  def formatAndSendMail(incoming: Incoming, buckets: Set[BucketId], interestingInfo: List[String]) {
    // Find the config whose filter matches
    val routeConfig = Config.opt(_.getConfigList("email.routes").asScala)
    val route = routeConfig.map(IncomingFilter.checkFilters(incoming, _)).flatten.toList.map(_.matchedConfig)

    // Extract the to and cc string list fields from the matching config if available
    val additionalTo = route.map(Config.opt(_, _.getStringList("to").asScala.toList)).toList.flatten.flatten
    val additionalCc = route.map(Config.opt(_, _.getStringList("cc").asScala.toList)).toList.flatten.flatten

    val subject = incoming.msgs.head.substring(0, math.min(incoming.msgs.head.length, 50))
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
      interestingInfo.mkString("\n"),
      incoming.flatBacktrace.mkString("\n")))
  }
}
