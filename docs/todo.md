# Tasks

## v0.0.1

* [x] Chunked encoding for responses
* [x] Remove chunk aggregation for requests
* [ ] Investigate thread safety of HttpResponseEncoder
* [ ] Investigate threading model for Netty
* [ ] Exception handling - test throwing exception from each request method.
* [x] Exception handling - test providing invalid param's for each response method.
* [ ] Exception handling - client disconnects early
* [ ] Exception handling - invalid request
* [ ] Exception handling - Connection#onOpen throws exception
* [x] Logging
* [ ] FIXMEs
* [ ] TODOs

## v0.0.2

* [ ] KeepAlive support
* [ ] 100 Continue support

## v0.0.3

* [ ] Sendfile support
* [ ] Inputstreams for response body (see ChunkedWriteHandler).
