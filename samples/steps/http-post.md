# HTTP POST Step

The POST step allows for making POST requests with the input information.

```
post_step:
  call: http.post
  args:
    url: https://www.example-url.com
    contentType: plaintext
    plaintext: "value"
    body:
      test: "param"
  result: responseVariable
```

**Mandatory fields:**

* `call`, with value `http.post` - determines the step type
* `args`
    * `url` - the desired resource to query
    * `body`
        * *..desired body values* - Scripts can be used for body values

**Optional fields:**

* `args`
    * `query`
        * *..desired query values* - Scripts can be used for query values
    * `headers`
        * *..desired header values* - Scripts can be used for headers values
    * `result` - name of the variable to store the response of the query in, for use in other steps
    * `contentType` - alias of the MIME contenttype to use, currently allowed "plaintext" which maps 
        to 'text/plain' for plaintext or 'application/x-www-form-urlencoded' for maps.
        If left empty, 'application/json' will be used.
    * `plaintext` - used instead of `body` if a singular plaintext value is needed to be sent 

***Note: POST step responses are stored the same way as [GET step responses](./http-get.md#How-responses-are-stored-with-the-result-field)***

## Examples

### Standard POST step with its result used in other step

[`post-with-used-response.yml`](../../DSL/POST/steps/post/post-with-used-response.yml)

```
post_step:
  call: http.post
  args:
    url: http://localhost:8080/scripting/passing-post-parameters
    body:
      project: "byk"
      website: "krat.ee"
  result: the_message

return_value:
  return: ${the_message.response}
```

### POST step with payload sent as plaintext value

[`post-with-plaintext-value.yml`](../../DSL/POST/steps/post/post-with-plaintext-value.yml)

```
post_step:
  call: http.post
  args:
    url: http://localhost:8080/scripting/passing-post-parameters
    contentType: plaintext
    plaintext: 
        "byrokratt"
  result: the_message

return_value:
  return: ${the_message.response}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
