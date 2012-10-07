#!/usr/bin/python
import sys
import subprocess
import time
import optparse
import httplib
import math
import json



def post(connection, value_str, multi):
  s = time.time()
  endpoint = '/api/notice'
  if multi:
    endpoint = '/api/multi-notice'
  connection.request('POST', endpoint, value_str)
  res = connection.getresponse()
  print res.read()
  if res.status != 200:
    print 'Got a %s for %s ' % (res.status, value_str)
  return time.time() - s

if __name__ == '__main__':
  parser = optparse.OptionParser('usage: %s file [options]' % sys.argv[0])
  parser.add_option('--sleep', dest='sleep', help='wait between requests (secs)')
  parser.add_option('--skip', dest='skip', help='lines to skip')
  parser.add_option('--multi', dest='multi', help='use multi-notice, using MULTI notices per POST')
  parser.add_option('--now', action='store_true', dest='now', help='ignore notice date')
  (options, args) = parser.parse_args(args=sys.argv)
  if len(args) != 2:
    print parser.print_usage()
    sys.exit(1)
  
  connection = httplib.HTTPConnection('localhost:8080')

  sleep = 0
  if options.sleep:
    sleep = float(options.sleep)
  skip = 0
  if options.skip:
    skip = int(options.skip)

  if options.multi:
    every = int(options.multi)
  else:
    every = 1

  buf = []
  times = []
  try:
    fh = open(args[1])
    i = 0
    
    for l in fh:
      i += 1
      if i < skip:
        continue
      if sleep:
        time.sleep(sleep)

      if options.now:
        try:
          o = json.loads(l)
          del o['d']
          buf.append(json.dumps(o))
        except KeyError:
          buf.append(l)
        except ValueError:
          print l
          buf.append(l)
      buf.append(l)

      if (i % every) == 0:
        if options.multi:
          notice = '[%s]' % ','.join(buf)
        else:
          notice = buf[0]
        times.append(post(connection, notice, options.multi)) 
        buf = []

  except KeyboardInterrupt:
    print
    avg = sum(times)/len(times)
    print 'avg: %06f' % avg
    print 'stdev: %06f' % math.sqrt(sum([(x - avg) * (x - avg) for x in times]) / len(times))
    print 'n: %d' % len(times)
