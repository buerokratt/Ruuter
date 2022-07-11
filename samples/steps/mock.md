# Mock Step

Use reflected requests to imitate to-be API calls that do not yet exist.

```
step_1:
    call: reflect.mock
    args:
        request:
            some: "request"
        response:
            project: "Bürokratt"
            website: "www.kratid.ee"
    result: reflected_request

step_2:
    result: ${reflected_request.body.project}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
