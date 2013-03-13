// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.filter.ProcessedIncoming
import com.twitter.util.Future

trait HasBackgroundActions {
  def backgroundActions: BackgroundActions
}

trait BackgroundAction {
  def postSave(processedIncoming: ProcessedIncoming): Future[Unit]
}

trait BackgroundActions {
  def postSave(processedIncoming: ProcessedIncoming): List[Future[Unit]]
}

