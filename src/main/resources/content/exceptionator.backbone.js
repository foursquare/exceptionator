if (typeof Exceptionator == 'undefined') { var Exceptionator = {}; }

Exceptionator.GraphSpans = {
  LAST_HOUR: 'hour',
  LAST_DAY: 'day',
  LAST_MONTH: 'month'
}

Exceptionator.ListTypes = {
  BUCKET_KEY: 'bucket_key',
  BUCKET_GROUP: 'bucket_group',
  SEARCH: 'search'
}

Exceptionator.GraphSpan = Exceptionator.GraphSpans.LAST_HOUR;
Exceptionator.Limit = 10;
Exceptionator.MouseDown = 0;


/*
 * Notice
 */
Exceptionator.Notice = Backbone.Model.extend({
  initialize: function() {
    if(this.get('d')) {
      this.set({d_fmt: Exceptionator.Notice.formatDate(this.get('d'))})
    }
    _.each(this.get('bkts'), function(bucket, name) {
      if (name in Exceptionator.Config.friendlyNames) {
        bucket['friendlyName'] = Exceptionator.Config.friendlyNames[name];
      } else {
        bucket['friendlyName'] = name;
      }
      bucket['nm_uri'] = encodeURIComponent(name);
      bucket['k_uri'] = encodeURIComponent(bucket.k);
    });
    _.each(this.get('bt'), function(backtrace, index) {
      this.get('bt')[index] = backtrace.split('\n');
    }, this);
  },
  histogram: function(bucket) {
    var fieldName;
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_HOUR) {
      fieldName = 'h';
    }
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_DAY) {
      fieldName = 'd';
    }
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_MONTH) {
      fieldName = 'm';
    }
    return this.get('bkts')[bucket]['h'][fieldName];
  }
},{
  formatDate: function(value) {
    "use strict";
    var d = new Date(value)
    if(d.clone().clearTime().equals(new Date().clearTime())) {
      return d.toString('h:mm:ss tt');
    } else {
      return d.toString('dddd, MMMM dd, yyyy h:mm:ss tt');
    }
  },

  emptyHistogram: function() {
    var dateNow = new Date()
    var stop = dateNow.getTime();
    stop -= dateNow.getUTCMilliseconds();
    stop -= dateNow.getUTCSeconds() * 1000;

    dateNow.setMilliseconds(0)
    dateNow.setSeconds(0);

    var step;
    var nSteps;
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_HOUR) {
      step = 60 * 1000;
      nSteps = 60;
    }
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_DAY) {
      stop -= dateNow.getUTCMinutes() * 60 * 1000;
      step = 60 * 60 * 1000;
      nSteps = 24;
    }
    if (Exceptionator.GraphSpan == Exceptionator.GraphSpans.LAST_MONTH) {
      stop -= dateNow.getUTCMinutes() * 60 * 1000;
      stop -= dateNow.getUTCHours() * 60 * 60 * 1000;
      step = 24 * 60 * 60 * 1000;
      nSteps = 30;
    }
    var start = stop - (step * nSteps);
    stop = stop + step;
    var histo = {};
    _.each(_.range(start, stop, step), function(timestamp) { histo[timestamp] = 0; });
    return histo;
  }
});


Exceptionator.NoticeView = Backbone.View.extend({
  tagName: "div",
  className: "exc exc_hidden",

  render: function() {
    this.$el.empty();
    this.$el.append(Exceptionator.Soy.notice({n: this.model.toJSON()}));

    return this;
  }
});


/*
 * NoticeList
 */
Exceptionator.NoticeList = Backbone.Collection.extend({

  model: Exceptionator.Notice,

  initialize: function(models, options) {
    if (options.bucketName && options.bucketKey) {
      this.listType = Exceptionator.ListTypes.BUCKET_KEY;
      this.bucketKey = options.bucketKey;
      this.bucketName = options.bucketName;
      this.urlPart = '/api/notices/' + encodeURIComponent(this.bucketName) + '/' + encodeURIComponent(this.bucketKey) + '?';
      this.id = this.bucketName.replace(/\W/g,'_') + '_' + this.bucketKey.replace(/\W/g,'_');
      if (this.bucketName in Exceptionator.Config.friendlyNames) {
        this.title = Exceptionator.Config.friendlyNames[this.bucketName];
      } else {
        this.title = this.bucketName;
      }
      this.title += ': ' + this.bucketKey;
    } else if (options.bucketName) {
      this.listType = Exceptionator.ListTypes.BUCKET_GROUP;
      this.bucketName = options.bucketName;
      this.urlPart = '/api/notices/' + encodeURIComponent(this.bucketName) + '?';
      this.id = this.bucketName.replace(/\W/g,'_');
      if (this.bucketName in Exceptionator.Config.friendlyNames) {
        this.title = Exceptionator.Config.friendlyNames[this.bucketName];
      } else {
        this.title = this.bucketName;
      }
    } else {
      this.query = options.query;
      this.listType = Exceptionator.ListTypes.SEARCH;
      this.urlPart = '/api/search?q=' + encodeURIComponent(this.query) + '&';
      this.id = '_search_' + this.query.replace(/\W/g,'_');
      this.title = 'search: ' + this.query;
    }
  },

  url: function() {
    return this.urlPart + 'limit=' + encodeURIComponent(Exceptionator.Limit);
  },

  timeReverseSorted: function() {
    return this.sortBy(Exceptionator.NoticeList.reverseTimeIterator_);
  },

  histograms: function() {
    var empty = Exceptionator.Notice.emptyHistogram();
    var seriesList = [];
    if (this.listType == Exceptionator.ListTypes.BUCKET_KEY) {
      var mostRecent =  this.min(Exceptionator.NoticeList.reverseTimeIterator_);
      if (mostRecent) {
        seriesList = [{label: '', values: mostRecent.histogram(this.bucketName)}];
      }
    }

    if (this.listType == Exceptionator.ListTypes.BUCKET_GROUP) {
      seriesList = _.map(this.timeReverseSorted(), function(model) {
        return {label: model.get('bkts')[this.bucketName]['k'], values: model.histogram(this.bucketName)};
      }, this);
    }

    var retval = [];
    _.each(seriesList, function(series) {
      var rawData = [];
      retval.push({label: series.label, data:rawData});
      _.each(_.extend({}, empty, series.values), function (value, timestamp) {
        if (empty.hasOwnProperty(timestamp)) {
          rawData.push([timestamp, value]);
        }
      });
    });
    return retval;
  }

}, {
  reverseTimeIterator_: function(notice) {
    return -notice.get('d');
  }
});


Exceptionator.NoticeListView = Backbone.View.extend({

  tagName: "div",

  events: {
    "click .exc_header": "toggleBody"
  },

  toggleBody: function(e) {
    $(e.target).parents('.exc').toggleClass('exc_hidden');
  },

  getTitleEl: function() {
    return $('#title_' + this.id, this.el);
  },

  getGraphEl: function() {
    return $('#plot_' + this.id, this.el);
  },

  getListEl: function() {
    return $('#' + this.id, this.el);
  },

  initialize: function(options) {
    this.collection.on('add', this.add, this);
    this.collection.on('reset', this.reset, this);
    this.id = 'view_' + this.collection.id;
    if (options.hasOwnProperty('showGraph')) {
      this.showGraph = options.showGraph;
    } else {
      this.showGraph = true;
    }

    if (this.collection.listType == Exceptionator.ListTypes.SEARCH) {
      this.showGraph = false;
    }
    if (options.hasOwnProperty('showList')) {
      this.showList = options.showList;
    } else {
      this.showList = true;
    }
    this.noticeViews = {};
  },

  reset: function() {
    this.getListEl().empty();
    this.addNotices(this.collection.models);
    this.trigger('Exceptionator:NoticeListView:reset', this);
  },

  add: function(toAdd) {
    if ($.isArray(toAdd)) {
      this.addNotices(toAdd);
    } else {
      this.addNotice(toAdd);
    }
  },

  addNotices: function(notices) {
    _.each(notices, function(notice) {
      this.addNotice(notice);
    }, this);
  },

  addNotice: function(notice) {
    var el;
    if (this.noticeViews.hasOwnProperty(notice.id)) {
      el = this.noticeViews[notice.id].el;
    } else {
      var view = new Exceptionator.NoticeView({model: notice});
      this.noticeViews[notice.id] = view;
      el = view.render().el;
    }
    this.getListEl().append(el);
  },

  renderGraph: function() {
    if (this.showGraph) {
      var options = {
        lines: { show: true },
        legend: { position: "nw" },
        xaxis: { mode: "time", timezone: "browser" }
      };
      $.plot(
          this.getGraphEl(),
          _.first(this.collection.histograms(), Exceptionator.NoticeListView.MAX_GRAPH_LINES),
          options);
    }
    return this;
  },

  render: function() {
    this.$el.empty();
    this.$el.append(Exceptionator.Soy.noticeList({
      id: this.id,
      showList: this.showList,
      showGraph: this.showGraph,
      title: this.collection.title}));
    this.reset();
    return this;
  }

}, {
  MAX_GRAPH_LINES: 8,
});

Exceptionator.AppView = Backbone.View.extend({
  initialize: function(options) {
    this.noticeListViews = [];
    if (options.initialConfigs) {
      _.each(options.initialConfigs, this.addList, this);
    }
  },

  clear: function() {
    this.noticeListViews = [];
    this.canRenderGraphs = false;
  },

  addList: function(listOptions) {
    var list = new Exceptionator.NoticeList([], listOptions.list);
    var view = new Exceptionator.NoticeListView(_.extend({collection: list}, listOptions.view));
    view.on('Exceptionator:NoticeListView:reset', this.renderGraph, this);
    this.noticeListViews.push(view);
  },

  render: function() {
    this.$el.empty();
    _.each(this.noticeListViews, function(view) {
      this.$el.append(view.render().el);
    }, this);
    this.canRenderGraphs = true;
    return this;
  },

  renderGraph: function(view) {
    if (this.canRenderGraphs) {
      view.renderGraph();
    }
  },

  renderGraphs: function() {
    _.each(this.noticeListViews, function(view) {
      this.renderGraph(view);
    }, this);
    return this;
  },

  fetch: function() {
    _.each(this.noticeListViews, function(view) {
      view.collection.fetch();
    });
  }
});

Exceptionator.Routing = Backbone.Router.extend({
  initialize: function(options) {
    this.app = options.app;
    this.homepageConfig = options.homepage;
  },

  routes: {
    "":                               "index",
    "search/?q=:query":               "search",
    "notices/:bucketName/:bucketKey": "bucket",
    "notices/:bucketName":            "bucketGroup"
  },

  route_: function(listDefs) {
    this.app.clear();
    _.each(listDefs, function(listDef) {
      this.app.addList(listDef);
    }, this);
    this.app.render();

    if (Exceptionator.Paused) {
      this.app.fetch();
    }
  },

  index: function() {
    this.route_(this.homepageConfig);
  },

  search: function(query) {
    this.route_([{list: {query: decodeURIComponent(query).toLowerCase()}}]);
  },

  bucket: function(bucketName, bucketKey) {
    this.route_([{list: {bucketName: decodeURIComponent(bucketName), bucketKey: decodeURIComponent(bucketKey)}}]);
  },

  bucketGroup: function(bucketName) {
    this.route_([{list: {bucketName: decodeURIComponent(bucketName)}}]);
  }

});
