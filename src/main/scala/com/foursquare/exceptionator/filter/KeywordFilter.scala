// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.filter

import com.foursquare.exceptionator.model.io.{BucketId, Incoming}
import com.twitter.finagle.Service

abstract class KeywordFilter extends PreSaveFilter {
  def register(registry: Registry) {}

  def apply(incoming: FilteredIncoming, service: Service[FilteredIncoming, ProcessedIncoming]) = {
    val newIncoming = incoming.copy(keywords=(incoming.keywords ++ keywords(incoming)))
    service(newIncoming)
  }

  def keywords(incoming: FilteredIncoming): Set[String]
}

