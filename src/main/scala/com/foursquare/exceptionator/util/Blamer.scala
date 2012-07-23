// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.util

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import java.io.File
import scala.sys.process._
import scala.util.matching.Regex
import org.joda.time.DateTime

object Blame {
  def apply(filePath: String, lineNum: Int): Blame = Blame("", "", "", filePath, "", lineNum)
}

case class Blame(author: String, mail: String, date: String, filePath: String, lineString: String, lineNum: Int) {
  def valid_? = author.length > 0 && mail.length > 0 && mail != "not.committed.yet"
}

trait Blamer {
  def blame(tag: String, fileName: String, line: Int): List[Blame]
}


class ConcreteBlamer extends Blamer with Logger {
  val blamer = Config.opt(_.getString("git.repo"))
    .map(r => new File(r))
    .find(f => f.exists && f.isDirectory)
    .map(rf => new GitBlamer(rf)).getOrElse({
      logger.warning("git.repo isn't configured or is misconfigured.  Blame support disabled.")
      new NilBlamer
    })

  def blame(tag: String, fileName: String, line: Int): List[Blame] = blamer.blame(tag, fileName, line)

}


class NilBlamer extends Blamer {
  def blame(tag: String, fileName: String, line: Int): List[Blame] = Nil
}

class GitBlamer(val root: File) extends Blamer with Logger {

  val maxTags = 4
  val rootSub = if(root.toString.endsWith("/")) root.toString.length else root.toString.length + 1
  val recentTags =
    new ConcurrentLinkedHashMap.Builder[String, List[(String, String)]].maximumWeightedCapacity(maxTags).build()

  def checkout(tag: String) {
    if (tag == "") {
      return
    }
    try {
      val gdescribe = Process(List("git", "describe", "--tags"), root)
      var currTag = ""
      val ret = gdescribe ! ProcessLogger((s) => currTag = s)
      if (currTag != tag) {
        val gco = Process(List("git", "checkout", tag), root)
        val ret = gco ! ProcessLogger((s) => Unit, (s) => logger.info(s))
        if (ret != 0) {
          val gfetch = Process(List("git", "fetch"), root)
          val ret0 = gfetch ! ProcessLogger((s) => Unit, (s) => logger.info(s))
          if (ret0 != 0) {
            throw new RuntimeException("couldn't fetch!")
          }
          val gfetchTags = Process(List("git", "fetch", "--tags"), root)
          val ret1 = gfetchTags ! ProcessLogger((s) => Unit, (s) => logger.info(s))
          if (ret1 != 0) {
            throw new RuntimeException("couldn't fetch tags!")
          }
          val ret2 = gco ! ProcessLogger((s) => Unit, (s) => logger.info(s))
          if (ret2 != 0) {
            throw new RuntimeException("couldn't fetch tag " + tag)
          }
        }
      }
    } catch {
      case e =>
        logger.error(e, "Couldn't check out revision " + tag)
        throw e
    }
  }

  def findFile(tag: String, name: String): List[String] = {
    def buildListRec(f: File):List[(String, String)] = f.isDirectory match {
      case true => f.listFiles.toList.map(f => buildListRec(f)).flatten
      case false => List((f.getName -> f.toString.substring(rootSub)))
    }
    val l = recentTags.get(tag)
    if (l != null) {
      l.filter(_._1 == name).map(_._2)
    }
    else {
      checkout(tag)
      logger.info("building git tree...")
      val l = buildListRec(root)
      recentTags.put(tag, l)
      l.filter(_._1 == name).map(_._2)
    }
  }

  def blame(tag: String, fileName: String, line: Int): List[Blame] = {
    val Author = new Regex("""^author (.*)$""")
    val Mail = new Regex("""^author-mail <(.*)>$""")
    val Date = new Regex("""^committer-time (.*)$""")
    val Summary = new Regex("""^summary (.*)$""")
    val LineString = new Regex("^\t(.*)$")
    def updateBlame(line: String, b: Blame) = line match {
      case Author(a) => b.copy(author=a)
      case Date(a) => b.copy(date=new DateTime(a.toLong * 1000L).toString)
      case Mail(m) => b.copy(mail=m)
      case LineString(l) => b.copy(lineString=l)
      case _ => b
    }
    checkout(tag)
    findFile(tag, fileName).map( f => {
      var blameObj = Blame(f, line)
      val gblame = Process(List("git", "blame", "-w", "--porcelain", "-L%d,%d".format(line, line), f), root)
      val plogger = ProcessLogger ((s) => blameObj = updateBlame(s, blameObj), (s) => logger.info(s))
      val ret = gblame ! plogger
      blameObj
    })
  }
}
