// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.filter.{BucketSpec, Registry, ProcessedIncoming, FilteredIncoming}
import com.twitter.util.Future


trait IncomingActions extends Registry {
  def registerBucket(bucket: BucketSpec)
  def bucketFriendlyNames: Map[String, String]
  def apply(incoming: FilteredIncoming): Future[ProcessedIncoming]
}
