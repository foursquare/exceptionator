// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.filter.concrete

import com.foursquare.exceptionator.filter.{BucketFilter, FilteredIncoming, KeywordFilter, TagFilter}
import com.foursquare.exceptionator.model.io.BacktraceLine
import com.foursquare.exceptionator.util.Hash

// Some basic filters that should be interesting to everyone

class AllBucketFilter extends BucketFilter {
  def name = "all"
  def friendlyName = "all"
  override def maxRecent = 30
  override def invalidatesFreshness = false
  def key(incoming: FilteredIncoming) = Some("all")
}

class StackBucketFilter extends BucketFilter {
  def name = "s"
  def friendlyName = "stack"
  def key(incoming: FilteredIncoming): Option[String] =
    Some(Hash.ofList(incoming.incoming.structuredBacktrace.flatMap(_.map(_.method))))
}

class StackPrefixBucketFilter extends BucketFilter {
  def name = "sp"
  def friendlyName = "stack prefix"
  def key(incoming: FilteredIncoming) = {
    incoming.incoming.flatBacktrace.takeWhile(l => !incoming.incoming.isInteresting(l)) match {
      case Nil => None
      case prefix => Some(Hash.ofList(prefix.map(BacktraceLine(_).method)))
    }
  }
}

class VersionBucketFilter extends BucketFilter {
  def name = "v"
  def friendlyName = "version"
  override def invalidatesFreshness = false
  def key(incoming: FilteredIncoming) = Option(incoming.incoming.v).filter(_ != "")
}

// This has a special use.  It is added only for all buckets which invalidate freshness, this
// notice is the first create a key for that bucket.
object FreshBucketFilter extends BucketFilter {
  def name = "fresh"
  def friendlyName = "fresh"
  override def maxRecent = 100
  override def invalidatesFreshness = false
  def key(incoming: FilteredIncoming)  = Some("fresh")
}

class IncomingTagsFilter extends TagFilter {
  def tags(incoming: FilteredIncoming): Set[String] = incoming.incoming.tags.map(_.toSet).getOrElse(Set())
}

// Probably want to place this on the filter stack below all tag filters
class TagsKeywordFilter extends KeywordFilter {
  def keywords(incoming: FilteredIncoming): Set[String] = incoming.tags
}

class BucketsKeywordFilter extends KeywordFilter {
  def keywords(incoming: FilteredIncoming): Set[String] = {
    incoming.buckets.filterNot(_.name == "all").map(id => "bucket_%s_%s".format(id.name, id.key))
  }
}
