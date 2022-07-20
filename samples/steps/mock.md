# Mock Step

The mock step allows to imitate to-be API calls, that do not yet exist.

**Mandatory fields:**

* `call`, with value `reflect.mock` - determines the step type
* `args`
    * response
        * *...response values*
* `result` - name of the variable to store the response of the mock in, for use in other steps

**Optional fields:**

* `args`
    * request
        * *url*
        * *query*
        * *body*
        * *headers*

#### How responses are stored with the result field

The mock step imitates the result of a http step - therefore results are stored into the application context the same way as with http
steps: [GET step responses](./http-get.md#How-responses-are-stored-with-the-result-field)

## Examples

###

```
step_1:
    call: reflect.mock
    args:
        request:
            url: https://www.example-url.com
        response:
            project: "Bürokratt"
            website: "www.kratid.ee"
    result: reflected_request

step_2:
    return: ${reflected_request.response}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
