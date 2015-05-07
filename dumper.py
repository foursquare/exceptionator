from BaseHTTPServer import BaseHTTPRequestHandler
import json
import optparse
import SocketServer
import sys


class JsonDumpHandler(BaseHTTPRequestHandler):
  def do_POST(self):
    content_len = int(self.headers.getheader('content-length', 0))
    raw = self.rfile.read(content_len)

    if self.path == '/api/notice':
      sys.stdout.write(raw)
    elif self.path == '/api/multi-notice':
      try:
        for notice in json.loads(raw):
          json.dump(notice, sys.stdout)
          sys.stdout.write('\n')
      except Exception:
        pass

    sys.stdout.flush()
    self.send_response(200)


if __name__ == '__main__':
  parser = optparse.OptionParser('usage: %s [options]' % sys.argv[0])
  parser.add_option('--port', dest='port', help='listen on port PORT')
  (options, args) = parser.parse_args(args=sys.argv)

  port = 8080
  if options.port:
    port = int(options.port)

  httpd = SocketServer.TCPServer(("localhost", port), JsonDumpHandler)
  httpd.serve_forever()
