// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.util

import java.util.concurrent.{ConcurrentHashMap, Executors}
import java.util.concurrent.atomic.AtomicInteger
import scala.util.matching.Regex
import com.twitter.ostrich.stats.Stats

object RollingRank {
  def apply(name: String): RollingRank =
    new RollingRank(
      name,
      Config.opt(_.getString("rollingRank.tokenizers")).getOrElse("\\s+"),
      Config.opt(_.getString("rollingRank.filter")).map(new Regex(_)),
      Config.opt(_.getString("rollingRank.filterNot")).map(new Regex(_)),
      Config.opt(_.getInt("rollingRank.windowSize")).getOrElse(1000))
}


class RollingRank (
    name: String,
    tokenizers: String,
    filter: Option[Regex],
    filterNot: Option[Regex],
    windowSize: Int) {

  val seenDocs = new AtomicInteger
  val docs = new AtomicInteger
  val docFrequency = new ConcurrentHashMap[String, AtomicInteger]()
  val window = Array.fill(windowSize)(Iterable[String]())
  Stats.addGauge("ir.%s.docFrequency.size".format(name)) { docFrequency.size() }


  def prune(old: Iterable[String]) {
    old.foreach(term => {
      val decremented: Option[Int] = Option(docFrequency.get(term)).map(_.addAndGet(-1))
      if (decremented.exists(_ == 0)) {
        docFrequency.remove(term)
      }
    })
    if (!old.isEmpty) {
      docs.addAndGet(-1)
    }
  }

  def rank(doc: String): List[(String, Double)] = {
    Stats.incr("ir.%s.seenDocs".format(name))
    //TODO: per-field tokenizers?
    val terms = doc.split(tokenizers)
      .filterNot(t => filterNot.map(_.pattern.matcher(t).matches).getOrElse(false))
      .filter(t => filter.map(_.pattern.matcher(t).matches).getOrElse(true)).map(_.toLowerCase)
    val termCounts: Map[String, Int] = terms.toList.groupBy(term => term).map { case (k, v) => k -> v.length }

    val currDocs = docs.addAndGet(1)
    val windowPos = seenDocs.addAndGet(1) % windowSize
    prune(window(windowPos))
    window(currDocs % windowSize) = termCounts.keys

    val res = termCounts.toList.map { case (term, count) => {
      val currDf = Option(docFrequency.putIfAbsent(term, new AtomicInteger(1))).map(_.addAndGet(1)).getOrElse(1)
      val tf = count.toDouble / terms.length
      val idf = math.log(currDocs.toDouble / currDf)
      val tfidf: Double = tf * idf
      term -> tfidf
    }}
    res.sortBy(-_._2)
  }
}
