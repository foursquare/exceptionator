// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.io.{BucketId, Incoming, Outgoing}
import org.bson.types.ObjectId

trait NoticeActions {
  def get(ids: List[ObjectId]): List[Outgoing]
  def search(keywords: List[String], limit: Option[Int]): List[Outgoing]
  def save(incoming: Incoming, tags: Set[String], keywords: Set[String], buckets: Set[BucketId]): ObjectId
  def addBucket(id: ObjectId, bucketId: BucketId): Unit
  def removeBucket(id: ObjectId, bucketId: BucketId): Unit
}
