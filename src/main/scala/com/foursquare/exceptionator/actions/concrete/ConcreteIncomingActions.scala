// Copyright 2012 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.actions.concrete

import com.foursquare.exceptionator.actions.{IncomingActions, BucketActions, NoticeActions}
import com.foursquare.exceptionator.filter.{BucketSpec, FilteredIncoming, FilteredSaveService,
    PreSaveFilter, ProcessedIncoming}
import com.foursquare.exceptionator.filter.concrete.FreshBucketFilter
import com.foursquare.exceptionator.model.io.BucketId
import com.foursquare.exceptionator.util.{Config, Logger, PluginLoader}
import com.twitter.ostrich.stats.Stats
import com.twitter.finagle.Service
import com.twitter.util.{Future, FuturePool}
import java.util.concurrent.Executors
import org.bson.types.ObjectId
import scalaj.collection.Imports._


class FilteredConcreteIncomingActions(service: Service[FilteredIncoming, ProcessedIncoming] with IncomingActions)
  extends Service[FilteredIncoming, ProcessedIncoming] with IncomingActions {

  def bucketFriendlyNames = service.bucketFriendlyNames
  def registerBucket(spec: BucketSpec) {
    service.registerBucket(spec)
  }
  val filters = PluginLoader.defaultConstruct[PreSaveFilter](Config.root.getStringList("incoming.savefilters").asScala)
  service.registerBucket(FreshBucketFilter)
  val filteredService = new FilteredSaveService(service, filters.toList, service)

  def apply(incoming: FilteredIncoming): Future[ProcessedIncoming] = filteredService(incoming)
}

class ConcreteIncomingActions(noticeActions: NoticeActions, bucketActions: BucketActions)
    extends Service[FilteredIncoming, ProcessedIncoming] with IncomingActions with Logger {

  val saveFuturePool = FuturePool(Executors.newFixedThreadPool(10))
  val bucketSpecs = scala.collection.mutable.Map[String, BucketSpec]()
  val incomingFilters = Config.opt(_.getConfigList("incoming.filters").asScala).toList.flatten

  var currentTime: Long = 0
  var lastHistogramTrim: Long = 0

  def registerBucket(spec: BucketSpec) {
    bucketSpecs += spec.name -> spec
  }

  def bucketFriendlyNames = bucketSpecs.toMap.map {
    case (name, builder) => name -> builder.friendlyName
  }

  def apply(incoming: FilteredIncoming): Future[ProcessedIncoming] = {
    saveFuturePool({
      save(incoming)
    })
  }

  def save(incoming: FilteredIncoming): ProcessedIncoming = {

    val tags = incoming.tags
    val kw = incoming.keywords
    val buckets = incoming.buckets


    val incomingId = noticeActions.save(incoming.incoming, tags, kw, buckets)

    // Increment /create buckets
    val results = buckets.map(bucket => {
      val max = bucketSpecs(bucket.name).maxRecent
      bucketActions.save(incomingId, incoming.incoming, bucket, max)
    })

    // As long as nothing that already exists invalidates freshness, we call it fresh
    val finalBuckets = {
      if (!results.exists(r => r.oldResult.isDefined &&
          bucketSpecs(r.bucket.name).invalidatesFreshness)) {

        val freshKey = BucketId(FreshBucketFilter.name, FreshBucketFilter.key(incoming).get)
        val res = bucketActions.save(incomingId, incoming.incoming, freshKey, FreshBucketFilter.maxRecent)

        Stats.time("incomingActions.add") {
          noticeActions.addBucket(incomingId, freshKey)
        }
        buckets + freshKey
      } else {
        buckets
      }
    }

    val remove = results.flatMap(r => r.noticesToRemove.map(_ -> r.bucket)).toList

    // A bit racy, but only approximation is needed.  Want to trim histograms
    // about every hour
    val now = incomingId.getTime
    if (now > currentTime) {
      currentTime = now
      if (currentTime - lastHistogramTrim > (60/*mins*/ * 60/*secs*/ * 1000)) {
        lastHistogramTrim = now
        bucketActions.deleteOldHistograms(now, true)
      }
    }

    // Fix up old notices that have been kicked out
    Stats.time("incomingActions.remove") {
      remove.foreach(bucketRemoval => noticeActions.removeBucket(bucketRemoval._1, bucketRemoval._2))
    }

    ProcessedIncoming(Some(incomingId), incoming.incoming, tags, kw, finalBuckets)
  }
}
