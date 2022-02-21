```
incoming_requests:
  headers:
    X-Forwarded-For: ${incoming.headers.remote_addr}
  external_forwarding:
    method: POST
    endpoint: "https://turvis/ruuter-incoming"
    params_to_pass:
      GET: true
      POST: true
      headers: true
    proceed_predicate:
      http.status.code: [200..202]

stop_processing_unresponding_steps: true

http_post:
  headers:
    Content-Type: "application/json"

services_without_response:
  headers:
    http.status.code: 200

final_response:
  headers:
    http.status.code: 200
```