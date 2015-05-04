// Copyright 2015 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.io.Incoming

trait HasExceptionActions {
  def exceptionActions: ExceptionActions
}

trait ExceptionActions extends IndexActions {
  def save(incoming: Incoming): String
}
