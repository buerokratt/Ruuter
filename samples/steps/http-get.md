# HTTP GET Step

The GET step allows for making GET requests.

```
get_step:
  call: http.get
  args:
    url: https://www.example-url.com
  result: responseVariable
```

**Mandatory fields:**

* `call`, with value `http.get` - determines the step type
* `args`
    * `url` - the desired resource to query

**Optional fields:**

* `args`
    * `query`
        * *..desired query values*
    * `headers`
        * *..desired header values*
    * `result` - name of the variable to store the response of the query in, for use in other steps

#### How responses are stored with the result field

If a `result` field is valued, then both the request parameters and request response are stored into a variable with the designated name.

Request parameters are stored under the `request` field

Request response is stored under the `response` field

The resulting object looks like this, assuming that the field is named `getStepResult`:

```
{
    "getStepResult": {
        "request": {
            "url": "https://www.example-url.com",
            "query": null,
            "headers": null,
            "body": null
        },
        "response": {
            "body": {
                ...body
            },
            "headers": {
                ...headers
            },
            "status": 200
        }
    }
}
```

## Examples

### Standard GET request

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: "Hello World"
      another_val: 123
  result: the_message
```

### GET request with custom header and without query parameters

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message
```

### GET request with variables

```
assign_values:
    assign:
        stringValue: "Bürokratt"
        integerValue: 2021

get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: ${stringValue}
      another_val: ${integerValue}
  result: the_message
```

### Using GET request response

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message

return_value:
  return: ${the_message.response}
```

### Using GET request parameters

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message

return_value:
  return: ${the_message.request}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
