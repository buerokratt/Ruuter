# HTTP POST Step

The POST step allows for making POST requests with the input information.

```yaml
post_step:
  call: http.post
  args:
    url: https://www.example-url.com
    contentType: plaintext
    plaintext: "value"
    body:
      test: "param"
  limit: 500
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
    * `contentType` - specifies the contenttype to use, currently allowed values:
      * `"plaintext"` - uses field `plaintext` and mediaType 'text/plain'
      * `"formdata"` 
        - if a key start with `file:`, that field content is sent as a file on a 
field named as the second part of the key and original filename as third part of 
the key, for example `file:projectdata:Project.csv`, and mediatype "multipart/form-data";   
        - otherwise maps `body` as url-encoded form and mediatype 'application/x-www-form-urlencoded' 
          as 
      * If left empty, `body` is posted as JSON and 'application/json' is used as mediatype.
    * `plaintext` - used instead of `body` if a singular plaintext value is needed to be sent 
* `limit` - limit the size of allowed response in kilobytes (default value is configured in application.yaml)

* ***Note: POST step responses are stored the same way as [GET step responses](./http-get.md#How-responses-are-stored-with-the-result-field)***

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

### POST step with formdata and file
```
post_step:
  call: http.post
  args:
    url: http://localhost:8080/scripting/passing-post-parameters
    contentType: formdata
    body:
      description: "Requested file"
      file:fieldname:requested.txt: > 
            This is the required content
            formatted as YAML multiline string   
  result: the_message

return_value:
  return: ${the_message.response}
```

### HTTP Error handling

It is possible to specify the DSL step to follow when POST request gets a
non-OK response. For that the `error` field can be used
```
post_step:
  call: http.post
  args:
    url: http://localhost:8080/nonexistant
    contentType: plaintext
    error: error_step
    plaintext: 
        "byrokratt"
  result: the_message

return_value:
  return: ${the_message.request}
  
error_step:
  return: "Request failed"
  status: 500  
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
