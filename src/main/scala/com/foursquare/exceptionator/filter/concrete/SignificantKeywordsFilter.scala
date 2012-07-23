// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.filter.concrete

import com.foursquare.exceptionator.filter.{FilteredIncoming, KeywordFilter}
import com.foursquare.exceptionator.util.{Config, IncomingFilter, RollingRank}
import scalaj.collection.Imports._

class SignificantKeywordsFilter extends KeywordFilter {
  val keywordProcessors = Config.opt(_.getStringList("incoming.keywordFields").asScala)
    .getOrElse(List("msgs", "excs", "bt")).map(f => f -> RollingRank(f)).toMap

  def keywords(incoming: FilteredIncoming): Set[String] = {
    keywordProcessors.toList.map({ case (field, ranker) => {
      val value = IncomingFilter.fieldGetter(field)(incoming.incoming).mkString("\n") // any whitespace will do
      ranker.rank(value).map(_._1).take(20)
    }}).flatten.toSet
  }
}
