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
  output.append('<a class="bucketLink" onclick="Exceptionator.Router.navigate(\'/notices/', soy.$$escapeHtml(opt_data.b.nm_uri), '/', soy.$$escapeHtml(opt_data.b.k_uri), '\', {trigger: true});">', soy.$$truncate(soy.$$escapeHtml(opt_data.b.k), 10, true), ' ', (opt_data.b.n) ? '(' + soy.$$escapeHtml(opt_data.b.n) + ')' : '', '</a>');
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
      Exceptionator.Soy.bucketLink({b: opt_data.n.bkts[kData59]}, output);
      output.append('</div>');
    }
  }
  output.append('</div>', (opt_data.n.h) ? '<div class="header">Host</div><div class="items"><div class="item">' + soy.$$escapeHtml(opt_data.n.h) + '</div></div>' : '', '<div class="header">Version</div><div class="items"><div class="item">', soy.$$escapeHtml(opt_data.n.v), '</div></div><div class="header">Session</div><div class="items">');
  var kList78 = soy.$$getMapKeys(opt_data.n.sess);
  var kListLen78 = kList78.length;
  for (var kIndex78 = 0; kIndex78 < kListLen78; kIndex78++) {
    var kData78 = kList78[kIndex78];
    output.append('<div class="item">', soy.$$escapeHtml(kData78), ': ', soy.$$escapeHtml(opt_data.n.sess[kData78]), '</div>');
  }
  output.append('</div><div class="header">Environment</div><div class="items">');
  var kList86 = soy.$$getMapKeys(opt_data.n.env);
  var kListLen86 = kList86.length;
  for (var kIndex86 = 0; kIndex86 < kListLen86; kIndex86++) {
    var kData86 = kList86[kIndex86];
    output.append('<div class="item">', soy.$$escapeHtml(kData86), ': ', soy.$$escapeHtml(opt_data.n.env[kData86]), '</div>');
  }
  output.append('</div><div class="header">Keywords</div><div class="items"><div class="item">');
  var kwList94 = opt_data.n.kw;
  var kwListLen94 = kwList94.length;
  for (var kwIndex94 = 0; kwIndex94 < kwListLen94; kwIndex94++) {
    var kwData94 = kwList94[kwIndex94];
    Exceptionator.Soy.kwLink({kw: kwData94}, output);
    output.append('  ');
  }
  output.append('</div></div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.noticeHeader = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div class="exc_header">');
  var kList102 = soy.$$getMapKeys(opt_data.n.bkts);
  var kListLen102 = kList102.length;
  for (var kIndex102 = 0; kIndex102 < kListLen102; kIndex102++) {
    var kData102 = kList102[kIndex102];
    if (kData102 != 'all') {
      output.append('<div class="exc_n" onclick="event.cancelBubble = true;">', soy.$$escapeHtml(opt_data.n.bkts[kData102].friendlyName), ' -  ');
      Exceptionator.Soy.bucketLink({b: opt_data.n.bkts[kData102]}, output);
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
  output.append('<div id="outer_', soy.$$escapeHtml(opt_data.id), '"><div class="row"><div class="span12"><h4 id="title_', soy.$$escapeHtml(opt_data.id), '">', soy.$$escapeHtml(opt_data.title), '</h4></div></div>', (opt_data.showGraph) ? '<div class="row"><div class="span12"><div id="plot_' + soy.$$escapeHtml(opt_data.id) + '" style="width:940px;height:170px"></div></div></div>' : '', (opt_data.showList) ? '<div class="row"><div class="span12"><div id="' + soy.$$escapeHtml(opt_data.id) + '"></div></div></div>' : '', '</div>');
  return opt_sb ? '' : output.toString();
};
