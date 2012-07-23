// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.
package com.foursquare.exceptionator.model.io

object BucketId {
  def apply(id: String): BucketId = id.split(":") match {
    case Array(name, key) => BucketId(name, key)
  }
}

case class BucketId(name: String, key: String) {
  override def toString = "%s:%s".format(name, key)
}
