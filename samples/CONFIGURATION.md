### DSL folder location
The location of the DSL folder, containing all of the DSL files, can be configured using the `application.config-path` property:
```
application:
    config-path: user/desired/location
```

### Exception handling
* `stopInCaseOfException` - defines whether DSL execution should be halted when an exception is encountered during processing or not.
  * if `true`, then on exception processing of further steps is halted and a response is immediately returned
  * if `false`, then processing of further steps continues and a response is returned when processing is done
```
application:
    stopInCaseOfException: true
```

### Incoming requests
* `incomingRequests.allowedMethodTypes` - defines the allowed method types for Ruuters DSL endpoint. Any request with an invalid method type ise "denied" service.
```
application:
  incomingRequests:
    allowedMethodTypes: [ POST, GET ]
```

### External forwarding
If the following values are defined, then every incoming request is forwarded to the defined endpoint.
* `method` - accepted values: `GET`, `POST`
* `paramsToPass.get` - whether to forward query params from incoming request
* `paramsToPass.post` - whether to forward body from incoming request
* `paramsToPass.headers` - whether to forward headers from incoming request
* `proceedPredicate.httpStatusCode` - defines the accepted http statuscodes for proceeding with configuration processing. If the external forwarding request
  receives a response not included in the list, then the request is not processed.
    * values can be defined as single status codes: `200`, `201`, `202`...
    * values can be defined as a range, using `..` special syntax: `200..202`
```
application:
    incomingRequests:
        externalForwarding:
            method: POST
            endpoint: "https://turvis/ruuter-incoming"
            paramsToPass:
                GET: true
                POST: false
                headers: true
            proceedPredicate:
                httpStatusCode: [ 200..202, 204 ]
```
