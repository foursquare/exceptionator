// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.model.io

import net.liftweb.json.{JValue, JArray, render}
import net.liftweb.json.JValue

object UserFilterView {
  def pretty(filters: List[UserFilterView]) = {
    net.liftweb.json.pretty(render(JArray(filters.map(_.doc))))
  }

  def compact(filters: List[UserFilterView]) = {
    net.liftweb.json.compact(render(JArray(filters.map(_.doc))))
  }
}
trait UserFilterView {
  def doc: JValue
  def pretty = net.liftweb.json.pretty(render(doc))
  def compact = net.liftweb.json.compact(render(doc))
}

