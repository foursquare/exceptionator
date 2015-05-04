// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.io.{BucketId, Incoming}
import org.bson.types.ObjectId

trait HasHistoryActions {
  def historyActions: HistoryActions
}

trait HistoryActions extends IndexActions {
  // def get(ids: List[ObjectId]): List[Outgoing]
  def save(exceptionId: String, incoming: Incoming, buckets: Set[BucketId]): ObjectId
  def addBucket(id: ObjectId, bucketId: BucketId): Unit
  def removeBucket(id: ObjectId, bucketId: BucketId): Unit
}
