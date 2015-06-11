// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.NoticeRecord
import com.foursquare.exceptionator.model.io.Outgoing
import org.joda.time.DateTime


trait HasHistoryActions {
  def historyActions: HistoryActions
}

trait HistoryActions extends IndexActions {
  def get(time: DateTime, limit: Int): List[Outgoing]
  def save(notice: NoticeRecord): DateTime
}
