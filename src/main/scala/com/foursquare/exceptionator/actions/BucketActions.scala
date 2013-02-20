// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.io.{BucketId, Incoming, Outgoing}
import org.bson.types.ObjectId
import org.joda.time.DateTime


case class SaveResult(bucket: BucketId, oldResult: Option[BucketId], noticesToRemove: List[ObjectId])

trait BucketActions {
  def get(ids: List[String], noticesPerBucketLimit: Option[Int], now: DateTime): List[Outgoing]
  def get(name: String, key: String, now: DateTime): List[Outgoing]
  def recentKeys(name: String, limit: Option[Int]): List[String]
  def save(incomingId: ObjectId, incoming: Incoming, bucket: BucketId, maxRecent: Int): SaveResult
  def deleteOldHistograms(time: Long, doIt: Boolean = true): Unit
  def deleteOldBuckets(lastUpdatedTime: Long, doIt: Boolean = true): List[SaveResult]
}
