// This file was automatically generated from exceptionator.soy.
// Please don't edit this file by hand.

if (typeof Exceptionator == 'undefined') { var Exceptionator = {}; }
if (typeof Exceptionator.Soy == 'undefined') { Exceptionator.Soy = {}; }


Exceptionator.Soy.kwLink = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<a class="bucketLink" onclick="Exceptionator.Router.navigate(\'/search/?q=', soy.$$escapeHtml(opt_data.kw), '\', {trigger: true});">', soy.$$escapeHtml(opt_data.kw), '</a>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.bucketLink = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<a class="bucketLink" onclick="Exceptionator.Router.navigate(\'/notices/', soy.$$escapeHtml(opt_data.nm), '/', soy.$$escapeHtml(opt_data.b.k), '\', {trigger: true});">', soy.$$truncate(soy.$$escapeHtml(opt_data.b.k), 10, true), ' ', (opt_data.b.n) ? '(' + soy.$$escapeHtml(opt_data.b.n) + ')' : '', '</a>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.bucketBody = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div class="header">First seen at</div><div class="items"><div class="item">', soy.$$escapeHtml(opt_data.b.df_fmt), '</div></div><div class="header">First seen version</div><div class="items"><div class="item">', soy.$$escapeHtml(opt_data.b.vf), '</div></div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.noticeBody = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div class="header">Message list</div><div class="items">');
  var iList31 = opt_data.n.msgs;
  var iListLen31 = iList31.length;
  for (var iIndex31 = 0; iIndex31 < iListLen31; iIndex31++) {
    var iData31 = iList31[iIndex31];
    output.append('<div class="item">', soy.$$escapeHtml(iData31), ' ', (! (iIndex31 == iListLen31 - 1)) ? ' Caused by: ' : '', '</div>');
  }
  output.append('</div><div class="header">Exception list</div><div class="items">');
  var iList41 = opt_data.n.excs;
  var iListLen41 = iList41.length;
  for (var iIndex41 = 0; iIndex41 < iListLen41; iIndex41++) {
    var iData41 = iList41[iIndex41];
    output.append('<div class="item">', soy.$$escapeHtml(iData41), ' ', (! (iIndex41 == iListLen41 - 1)) ? ' Caused by: ' : '', '</div>');
  }
  output.append('</div><div class="header">Backtrace</div><div class="items">');
  var btList51 = opt_data.n.bt;
  var btListLen51 = btList51.length;
  for (var btIndex51 = 0; btIndex51 < btListLen51; btIndex51++) {
    var btData51 = btList51[btIndex51];
    var iList52 = btData51;
    var iListLen52 = iList52.length;
    for (var iIndex52 = 0; iIndex52 < iListLen52; iIndex52++) {
      var iData52 = iList52[iIndex52];
      output.append('<div class="item">', soy.$$escapeHtml(iData52), '</div>');
    }
  }
  output.append('</div><div class="header">Matching buckets:</div><div class="items">');
  var kList59 = soy.$$getMapKeys(opt_data.n.bkts);
  var kListLen59 = kList59.length;
  for (var kIndex59 = 0; kIndex59 < kListLen59; kIndex59++) {
    var kData59 = kList59[kIndex59];
    if (kData59 != 'all') {
      output.append('<div class="item">', soy.$$escapeHtml(opt_data.n.bkts[kData59].friendlyName), ' -  ');
      Exceptionator.Soy.bucketLink({nm: kData59, b: opt_data.n.bkts[kData59]}, output);
      output.append('</div>');
    }
  }
  output.append('</div>', (opt_data.n.h) ? '<div class="header">Host</div><div class="items"><div class="item">' + soy.$$escapeHtml(opt_data.n.h) + '</div></div>' : '', '<div class="header">Version</div><div class="items"><div class="item">', soy.$$escapeHtml(opt_data.n.v), '</div></div><div class="header">Session</div><div class="items">');
  var kList79 = soy.$$getMapKeys(opt_data.n.sess);
  var kListLen79 = kList79.length;
  for (var kIndex79 = 0; kIndex79 < kListLen79; kIndex79++) {
    var kData79 = kList79[kIndex79];
    output.append('<div class="item">', soy.$$escapeHtml(kData79), ': ', soy.$$escapeHtml(opt_data.n.sess[kData79]), '</div>');
  }
  output.append('</div><div class="header">Environment</div><div class="items">');
  var kList87 = soy.$$getMapKeys(opt_data.n.env);
  var kListLen87 = kList87.length;
  for (var kIndex87 = 0; kIndex87 < kListLen87; kIndex87++) {
    var kData87 = kList87[kIndex87];
    output.append('<div class="item">', soy.$$escapeHtml(kData87), ': ', soy.$$escapeHtml(opt_data.n.env[kData87]), '</div>');
  }
  output.append('</div><div class="header">Keywords</div><div class="items"><div class="item">');
  var kwList95 = opt_data.n.kw;
  var kwListLen95 = kwList95.length;
  for (var kwIndex95 = 0; kwIndex95 < kwListLen95; kwIndex95++) {
    var kwData95 = kwList95[kwIndex95];
    Exceptionator.Soy.kwLink({kw: kwData95}, output);
    output.append('  ');
  }
  output.append('</div></div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.noticeHeader = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div class="exc_header">');
  var kList103 = soy.$$getMapKeys(opt_data.n.bkts);
  var kListLen103 = kList103.length;
  for (var kIndex103 = 0; kIndex103 < kListLen103; kIndex103++) {
    var kData103 = kList103[kIndex103];
    if (kData103 != 'all') {
      output.append('<div class="exc_n" onclick="event.cancelBubble = true;">', soy.$$escapeHtml(opt_data.n.bkts[kData103].friendlyName), ' -  ');
      Exceptionator.Soy.bucketLink({nm: kData103, b: opt_data.n.bkts[kData103]}, output);
      output.append('</div>');
    }
  }
  output.append('<div class="timehost">', soy.$$escapeHtml(opt_data.n.d_fmt), ' ', (opt_data.n.h) ? ' on ' + soy.$$escapeHtml(opt_data.n.h) + '</div> ' : '', ' ', soy.$$escapeHtml(opt_data.n.msgs[0]), '</div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.notice = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  Exceptionator.Soy.noticeHeader(opt_data, output);
  output.append('<div class="exc_detail" >');
  Exceptionator.Soy.noticeBody(opt_data, output);
  output.append('</div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.noticeList = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div id="outer_', soy.$$escapeHtml(opt_data.id), '"><div class="row"><div class="span16"><h3 id="title_', soy.$$escapeHtml(opt_data.id), '">', soy.$$escapeHtml(opt_data.title), '</h3></div></div>', (opt_data.showGraph) ? '<div class="row"><div class="span16"><div id="plot_' + soy.$$escapeHtml(opt_data.id) + '" style="width:940px;height:150px"></div></div></div>' : '', (opt_data.showList) ? '<div class="row"><div class="span16"><div id="' + soy.$$escapeHtml(opt_data.id) + '"></div></div></div>' : '', '</div>');
  return opt_sb ? '' : output.toString();
};
