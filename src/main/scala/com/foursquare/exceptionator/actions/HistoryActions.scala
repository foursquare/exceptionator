// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.NoticeRecord
import java.util.Date


trait HasHistoryActions {
  def historyActions: HistoryActions
}

trait HistoryActions extends IndexActions {
  def save(notice: NoticeRecord): Date
}
