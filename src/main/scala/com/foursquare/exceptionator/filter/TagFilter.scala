
// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.filter

import com.foursquare.exceptionator.model.io.{BucketId, Incoming}
import com.twitter.finagle.Service

abstract class TagFilter extends PreSaveFilter {
  def register(registry: Registry) {}

  def apply(incoming: FilteredIncoming, service: Service[FilteredIncoming, ProcessedIncoming]) = {
    val newIncoming = incoming.copy(tags=(incoming.tags ++ tags(incoming)))
    service(newIncoming)
  }

  def tags(incoming: FilteredIncoming): Set[String]
}
