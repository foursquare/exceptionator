// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.filter.ProcessedIncoming

trait BackgroundActions {
  def postSave(processedIncoming: ProcessedIncoming): Unit
}

