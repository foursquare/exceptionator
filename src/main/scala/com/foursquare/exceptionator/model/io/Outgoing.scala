// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.
package com.foursquare.exceptionator.model.io

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.bson.types.ObjectId

object Outgoing {
  def pretty(outgoings: List[Outgoing]) = {
    net.liftweb.json.pretty(net.liftweb.json.render(JArray(outgoings.map(_.doc))))
  }

  def compact(outgoings: List[Outgoing]) = {
    net.liftweb.json.compact(net.liftweb.json.render(JArray(outgoings.map(_.doc))))
  }
}


trait Outgoing {
  def doc: JValue
  def pretty = net.liftweb.json.pretty(net.liftweb.json.render(doc))
  def compact = net.liftweb.json.compact(net.liftweb.json.render(doc))
}
