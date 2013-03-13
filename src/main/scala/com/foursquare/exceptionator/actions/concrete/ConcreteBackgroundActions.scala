// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{BackgroundAction, BackgroundActions}
import com.foursquare.exceptionator.loader.service.HasPluginLoaderService
import com.foursquare.exceptionator.filter.ProcessedIncoming
import com.foursquare.exceptionator.util.{PluginLoader, Config}
import com.twitter.util.Future
import scalaj.collection.Imports._


class ConcreteBackgroundActions(
  services: HasPluginLoaderService) extends BackgroundActions {

  val actions = services.pluginLoader.serviceConstruct[BackgroundAction](
    Config.root.getStringList("incoming.postSaveActions").asScala)

  def postSave(processedIncoming: ProcessedIncoming): List[Future[Unit]] = {
    actions.map(_.postSave(processedIncoming)).toList
  }}
