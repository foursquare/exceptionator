// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions

import com.foursquare.exceptionator.model.io.{UserFilterView}
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait HasUserFilterActions {
  def userFilterActions: UserFilterActions
}

trait UserFilterActions {
  def getAll(userId: Option[String] = None): List[UserFilterView]
  def get(id: String): Option[UserFilterView]
  def remove(id: String, userId: Option[String])
  def save(jsonString: String, userId: String): Option[UserFilterView]
}
