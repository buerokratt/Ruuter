# HTTP GET Step

The GET step allows for making GET requests.

```yaml 
get_step:
  call: http.get
  args:
    url: https://www.example-url.com
  limit: 500
  result: responseVariable
```

**Mandatory fields:**

* `call`, with value `http.get` - determines the step type
* `args`
    * `url` - the desired resource to query
      * `contentType` - if set to `json_override` 
  overrides response content type with application/json.

**Optional fields:**

* `args`
    * `query`
        * *..desired query values* - Scripts can be used for query values
    * `headers`
        * *..desired header values* - Scripts can be used for headers values
* `result` - name of the variable to store the response of the query in, for use in other steps
  - __NB!__ if `result` field is missing, the request will be done immediately and
  non-blockingly, so the DSL execution does not wait for response. Any data that would
  be sent with response is not handled by Ruuter.
* `limit` - limit the size of allowed response in kilobytes (default value is configured in application.yaml) 
* `timeout`- (in milliseconds) overwrites http request timeout set in application properties (or if not
defined there, 15000ms)

#### How responses are stored with the result field

If a `result` field is valued, then both the request parameters and request response are stored into a variable with the designated name.

Request parameters are stored under the `request` field

Request response is stored under the `response` field

The resulting object looks like this, assuming that the `result` field is valued `getStepResult`:

```
{
    "getStepResult": {
        "request": {
            "url": ...,
            "query": ...,
            "headers": ...,
            "body": ...
        },
        "response": {
            "body": {
                ...body
            },
            "headers": {
                ...headers
            },
            "status": ...
        }
    }
}
```

## Examples

### Standard GET request

[`get.yml`](../../DSL/GET/steps/get/get.yml)

```
get_message:
  call: http.get
  args:
    url: http://localhost:8080/steps/return/return-with-script
    query:
      some_val: "Hello World"
      another_val: 123
  result: the_message
```

### GET request with custom header and without query parameters

[`get-with-header.yml`](../../DSL/GET/steps/get/get-with-header.yml)

```
get_message:
  call: http.get
  args:
    url: http://localhost:8080/steps/return/return-with-script
    headers:
      Content-Type: "text/plain"
  result: the_message
```

### GET request with variables

[`get-with-variable.yml`](../../DSL/GET/steps/get/get-with-variable.yml)

```
assign_values:
  assign:
    stringValue: "Bürokratt"
    integerValue: 2021

get_message:
  call: http.get
  args:
    url: http://localhost:8080/steps/return/return-with-script
    query:
      some_val: ${stringValue}
      another_val: ${integerValue}
  result: the_message

```

### Using GET request response

[`get-with-used-response.yml`](../../DSL/GET/steps/get/get-with-used-response.yml)

```
get_message:
  call: http.get
  args:
    url: http://localhost:8080/steps/return/return-with-script
  result: the_message

return_value:
  return: ${the_message.response}
```

### Using GET request parameters

[`get-with-used-request.yml`](../../DSL/GET/steps/get/get-with-used-request.yml)

```
get_message:
  call: http.get
  args:
    url: http://localhost:8080/steps/return/return-with-script
    query:
      var: "value"
  result: the_message

return_value:
  return: ${the_message.request}
```

### HTTP Error handling

It is possible to specify the DSL step to follow when GET request gets a
non-OK response. For that the `error` field can be used
```
get_message:
  call: http.get
  args:
    url: http://localhost:8080/guards/fail/mock-response #step that always gives 403
    query:
      var: "value"
  result: the_message
  error: error_step

return_value:
  return: ${the_message.request}
  
error_step:
  return: "Request failed"
  status: 500  
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
