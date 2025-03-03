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
* `incomingRequests.allowedMethodTypes` - defines the allowed method types for Ruuters DSL endpoint. Any request with an invalid method type is "denied" service.
```
application:
  incomingRequests:
    allowedMethodTypes: [ POST, GET ]
```

### Max step recursions
* `maxStepRecursions` - defines how many times can one step be executed to execute a step a specific number of times and to avoid infinite loops.
* When a step has reached its maximum recursions, DSL will continue from a next step that was not in a loop.
* Max recursions can also be defined on a step level, which will override this global limit only if it is smaller than the global limit.
* More information about step level max recursions [here](./general/max-recursions.md).
```
application:
    maxStepRecursions: 10
```

### External forwarding
If the following values are defined, then every incoming request is forwarded to the defined endpoint.
* `method` - accepted values: `GET`, `POST`
* `paramsToPass.get` - whether to forward query params from incoming request
* `paramsToPass.post` - whether to forward body from incoming request
* `paramsToPass.headers` - whether to forward headers from incoming request
* `paramsToPass.origin` - whether to forward original Ruuter endpoint
  * original endpoint and host is stored in query parameters `originalEndpoint` 
  and `originalHost` respectively
* `proceedPredicate.httpStatusCode` - defines the accepted http status codes for proceeding with DSL processing. If the external forwarding request
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
                origin: true
            proceedPredicate:
                httpStatusCode: [ 200..202, 204 ]
```

***Note!** `method`, `endpoint` and `proceedPredicate.httpStatusCode` must be defined for forwarding to work.*

### Final Response status code

Final response status code can be defined in application.yml. If defined, all final responses by Ruuter will respond with this status code no matter what.
There are two options. One is for DSLs that have a response and the other one is for DSLs that do not have a response.

The business value lies in not allowing perpetrators to fish for exceptions, weaknesses, etc in back-end systems via requests from public network.

Nevertheless, applications (including Ruuter's) log files contain actual HTTP response codes, being the appropriate source for debugging, alerting, etc.
```
finalResponse:
    dslWithResponseHttpStatusCode: 200
    dslWithoutResponseHttpStatusCode: 300
```

### DSL parameters

To make configuring DSL files easier it's possible to use custom parameters for frequently repeated values, for example domain url and server port.

In DSL `yaml` files the lines which use custom parameter must be inside quotes.
Example: `url: "[#DOMAIN_URL]:[#PORT]/steps/return/return-with-script"`

As shown above, entire line where custom parameters are used is surrounded with quotes.
If it's not surrounded in quotes, the program will fail to run.

Another example of how it works:
`url: "[#DOMAIN_URL]:[#PORT]/steps/return/return-with-script"` -> `url: "https://example.com:8080/steps/return/return-with-script"`

Parameters are defined in `constants.ini` file as `PARAM_NAME=VALUE`.
There is no limit to how many parameters you can define.

```
[DSL]
DOMAIN_URL=https://example.com
PORT=8080
```

### CORS

CORS CrossOrigin - whitelists domains which are permitted to access this application.
CORS configuration reads CrossOrigin URLs from `application.yml`.

```
application:
    CORS:
        allowedOrigins: [https://test.buerokratt.ee, https://admin.test.buerokratt.ee/, https://tim.test.buerokratt.ee/]
```

### Allowed DSL filetypes

Ruuter limits what filetypes are allowed in [`DSL`](../DSL) directory.
Allowed filetypes are defined in [application.yml](../src/main/resources/application.yml).

```
application:
    DSL:
        allowedFiletypes: [".yml", ".yaml", ".tmp"]
```

DSLs with `.tmp` filetype are inactive and not processed.
If [`DSL`](../DSL) directory contains any filetypes that are not defined above, then Ruuter will not start.

### Processed DSL filetypes

Ruuter only processes DSL files, which are defined in [application.yml](../src/main/resources/application.yml).
Everything not included will be considered inactive and will not be processed.

```
application:
    DSL:
        processedFiletypes: [".yml", ".yaml"]
```

### Reloading DSLs

It's possible to reload DSLs from the [`DSL directory`](../DSL), which is defined in `application.yml -> application -> config-path`
This feature can be enabled/disabled in [application.yml](../src/main/resources/application.yml).

```
application:
    DSL:
        allowDslReloading: {true / false}
```

DSL reloading can be called from any DSL step. Example:

```
my_step:
    reloadDsl: true
```

### Default service

Default service is a service file that will be executed when the request status code is not within the allowlist of HTTP response codes.

Default service can be defined in the `application.yml` file by the name of `defaultServiceInCaseOfException`. The service itself is yet another service in `/DSL/POST` folder. Both `body` and `query` parameters are optional and covered here as an example.

```
defaultServiceInCaseOfException:
    service: default-service
    body:
      someVal: "Hello World"
    query:
      anotherVal: 123
```


### Http request default headers

There is an example for POST requests. To add default headers to other requests in application.yml, define it in application.yml, also in
ApplicationProperties class the same way as HttpPost is defined and use addHeaders method from HttpQueryArgs to add these default headers to the request.


### Internal requests

Some DSL services can be exposed to internal requests as REST endpoints.
These can be accessed only from IPs that are specified in `allowedIPs` configuration block:
* `allowedIPs` - list of whitelisted IPs 
* `allowedURLs` - list of whitelisted referral URLs

```
  internalRequests:
    allowedIPs: ["127.0.0.1", "192.168.0.1", "172.21.0.1"]
    allowedURLs: ["http://localhost/internalTest"]
```


### Meaningful errors

To help debugging different problems in Ruuter and other components some extra
logging can be turned on.
* `(application,)logging.meaningfulErrors` - boolean

If this value is set to `true`, a specified error message written to
error log and also sent as a response to request with relevant error code.

### External logging

For further analysis, information about technical error can be stored to OpenSearch.
This logging can be turned on by adding configuration block to the application.yml:
```
  openSearchConfiguration:
    url: <opensearch server url>
    index: <index name for Ruuter logs>
```

Whenever an exception is thrown while executing any DSL step, an RuuterEvent object 
is written to Opensearch with fields:
```
    "timestamp": timestamp in milliseconds,
    "level": error level, "RUNTIME" for runtime DSL errors, "STARTUP" for startup parsing errors,
    "dslName": name of DSL where error occurred,
    "dslMethod": HTTP request method that triggered that DSL,
    "stepName": name of DSL step here error occurred,
    "statusCode": DSL HTTP return code (if applicable),
    "errorCode": DSL HTTP error code (if applicable),
    "requestParams": map of request parameters,
    "requestHeaders": map of request header parameters,
    "requestBody": map of request body values,
    "message": error message (if applicable),
    "stackTrace": Java stack trace for thrown exception.
```

### HTTP response size limit

Allows setting the limit for size of HTTP queries. 

Default value is 256 (kB).

```
    "httpResponseSizeLimit": response size limit in KB
```

### Allow duplicate request keys

Duplicate keys in request JSON is allowed by default. 
This functionality can be changed with setting `allowDuplicateRequestKeys` to false.
