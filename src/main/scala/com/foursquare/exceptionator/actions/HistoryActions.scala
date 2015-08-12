// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.{BucketRecord, NoticeRecord}
import com.foursquare.exceptionator.model.io.{BucketId, Outgoing}
import org.joda.time.DateTime


trait HasHistoryActions {
  def historyActions: HistoryActions
}

trait HistoryActions extends IndexActions {
  def get(bucketName: String, time: DateTime, limit: Int): List[Outgoing]
  def get(bucketName: String, bucketKey: String, time: DateTime, limit: Int): List[Outgoing]
  def get(ids: List[String], time: DateTime, limit: Int): List[Outgoing]
  def getGroupNotices(name: String, time: DateTime, limit: Int): List[NoticeRecord]
  def getNotices(buckets: List[String], time: DateTime, limit: Int): List[NoticeRecord]
  def oldestId: Option[DateTime]
  def save(notice: NoticeRecord): DateTime
}
