// Copyright 2013 Foursquare Labs Inc. All Rights Reserved.

package com.foursquare.exceptionator.filter
import com.foursquare.exceptionator.model.io.Incoming
import com.twitter.util.Future
import com.twitter.finagle.{Service, SimpleFilter}
import org.junit.Test
import org.specs._

class OrderedPreSaveFilter(id: String) extends PreSaveFilter {
  def register(registry: Registry) {}
  def apply(
    incoming: FilteredIncoming,
    service: Service[FilteredIncoming, ProcessedIncoming]
  ): Future[ProcessedIncoming] = {

    service(incoming.copy(tags = incoming.tags.map(_ + id)))
  }
}

class TestService extends Service[FilteredIncoming, ProcessedIncoming] {
  def apply(incoming: FilteredIncoming): Future[ProcessedIncoming] = {
    Future.value(ProcessedIncoming(
      None,
      incoming.incoming,
      incoming.tags,
      incoming.keywords,
      incoming.buckets))
  }
}

class FilteredSaveServiceTest extends SpecsMatchers {
  // Make sure that the service is built up in the proper order,
  // with the end of the PreSaveFilter list executing last
  // This allows the user to use filter ordering to observe
  // the transformations of earlier filters
  @Test
  def testFilterOrder {
    val incoming = FilteredIncoming(Incoming(
        Nil, Nil, Nil, Map(), Map(), "", "", None, None, None)).copy(tags = Set(""))

    val service = new FilteredSaveService(
      new TestService,
      List(
        new OrderedPreSaveFilter("1"),
        new OrderedPreSaveFilter("2"),
        new OrderedPreSaveFilter("3")),
      new Registry {
        def registerBucket(spec: BucketSpec) {}
      })

    service(incoming).poll.flatMap(_.toOption.map(_.tags)) must_== Some(Set("123"))
  }
}


