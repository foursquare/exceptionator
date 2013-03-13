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


Exceptionator.Soy.emptyNotice = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div class="exc_header" style="text-align:center;border-bottom-width:1px;border-bottom-style:solid">', soy.$$escapeHtml(opt_data.text), '</div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.noticeList = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div id="outer_', soy.$$escapeHtml(opt_data.id), '"><div class="row"><div class="span12"><h4 id="title_', soy.$$escapeHtml(opt_data.id), '">', soy.$$escapeHtml(opt_data.title), '</h4></div></div>', (opt_data.showGraph) ? '<div class="row"><div class="span12"><div id="plot_' + soy.$$escapeHtml(opt_data.id) + '" style="width:940px;height:170px"></div></div></div>' : '', (opt_data.showList) ? '<div class="row"><div class="span12"><div id="' + soy.$$escapeHtml(opt_data.id) + '"></div></div></div>' : '', '</div>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.userFilter = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<form><fieldset><legend></legend>', (opt_data.f._id) ? '<input type="hidden" name="_id" value="' + soy.$$escapeHtml(opt_data.f._id) + '" />' : '', '<label>Name</label><input type="text" name="n" value="', (opt_data.f.n) ? soy.$$escapeHtml(opt_data.f.n) : '', '" class="liveTitle" /><label>Owner: ', (opt_data.f.u) ? soy.$$escapeHtml(opt_data.f.u) : soy.$$escapeHtml(opt_data.u), '</label><input type="hidden" name="u" value="', (opt_data.f.u) ? soy.$$escapeHtml(opt_data.f.u) : '', '" />', (opt_data.f.lm && opt_data.f.lm_fmt) ? '<label>Last matched: ' + soy.$$escapeHtml(opt_data.f.lm_fmt) + '</label>' : '', (opt_data.f.mute_fmt) ? '<label>Muted until: ' + soy.$$escapeHtml(opt_data.f.mute_fmt) + ' &nbsp;<button type="button" class="btn" value="mute_0">Unmute</button></label>' : '', (opt_data.f._id) ? '<label> Mute: &nbsp;<button type="button" class="btn" value="mute_60">1 hour</button>&nbsp;<button type="button" class="btn" value="mute_1440">1 day</button></label>' : '', '<label>CC (one per line)</label><textarea rows="3" name="cc">');
  if (opt_data.f.cc) {
    var ccList189 = opt_data.f.cc;
    var ccListLen189 = ccList189.length;
    for (var ccIndex189 = 0; ccIndex189 < ccListLen189; ccIndex189++) {
      var ccData189 = ccList189[ccIndex189];
      output.append(soy.$$escapeHtml(ccData189), '\n');
    }
  }
  output.append('</textarea><label>Alert condition</label><select name="ft"><option value="kw" ', (opt_data.f.ft == 'kw') ? ' selected="selected" ' : '', '>keywords</option><!--<option value="b" ', (opt_data.f.ft == 'b') ? ' selected="selected" ' : '', '>buckets</option>--></select><label>Criteria to match (conjunct of space-separated terms)</label><textarea rows="3" name="c">');
  if (opt_data.f.c) {
    var critList204 = opt_data.f.c;
    var critListLen204 = critList204.length;
    for (var critIndex204 = 0; critIndex204 < critListLen204; critIndex204++) {
      var critData204 = critList204[critIndex204];
      output.append(soy.$$escapeHtml(critData204), ' ');
    }
  }
  output.append('</textarea><label>Alert condition</label><select name="tt" class="triggerType"><option value="always" ', (opt_data.f.tt == 'always') ? ' selected="selected" ' : '', '>always</option><option value="never" ', (opt_data.f.tt == 'never') ? ' selected="selected" ' : '', '>never (I\'m here for the bucket)</option><option value="pwr2" ', (opt_data.f.tt == 'pwr2') ? ' selected="selected" ' : '', '>on powers of 2</option><option value="threshold" ', (opt_data.f.tt == 'threshold') ? ' selected="selected" ' : '', '>threshold is crossed</option></select><div class="thresholdCfg" style="display:none;"><label>Threshold</label>If the exception rate exceeds &nbsp;<input type="text" name="tl" value="', (opt_data.f.tl || opt_data.f.tl == 0) ? soy.$$escapeHtml(opt_data.f.tl) : '', '" style="width: 6em;"/> exceptions per minute, &nbsp;<input type="text" name="tc" value="', (opt_data.f.tc || opt_data.f.tc == 0) ? soy.$$escapeHtml(opt_data.f.tc) : '', '"  style="width: 4em;"/> (&lt;=60) times in the last &nbsp;<input type="text" name="p" value="', (opt_data.f.p || opt_data.f.p == 0) ? soy.$$escapeHtml(opt_data.f.p) : '', '" style="width: 4em;"/> (&lt;=60) minutes, send an alert.</div><div>', (opt_data.f._id) ? '<button type="button" class="btn" value="update">Update</button><button type="button" class="btn" value="delete">Delete</button>' : '<button type="button" class="btn" value="save">Save</button>', '</div></fieldset></form>');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.userFilterCompact = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append((opt_data.f.n) ? soy.$$escapeHtml(opt_data.f.n) : '', ' -', (opt_data.f._id) ? ((opt_data.f.lm) ? '&nbsp; matched: ' + soy.$$escapeHtml(opt_data.f.lm_fmt) + ' -' : '') + '&nbsp; <a class="bucketLink" onclick="Exceptionator.Router.navigate(\'/notices/uf/' + soy.$$escapeHtml(opt_data.f._id) + '\', {trigger: true})">view bucket</a> - &nbsp;<a class="bucketLink" onclick="Exceptionator.Router.navigate(\'/filters/' + soy.$$escapeHtml(opt_data.f._id) + '\', {trigger: true})">edit</a>' : '');
  return opt_sb ? '' : output.toString();
};


Exceptionator.Soy.userFilterList = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<div><h4>', (opt_data.u) ? soy.$$escapeHtml(opt_data.u) + '\'s' : '', ' filters</h4><p>You can use filters to define buckets for one or more search terms, and receive an email based on the rate of occurance.  Buckets are a unit of aggregation for notices.  Their primary purpose is to provide a histogram so that the rate of occurance can be tracked.</p><div class="filters"></div><button class="btn filterAdd" value="add">add a new filter</button></div>');
  return opt_sb ? '' : output.toString();
};
