# Template Step

```
call_template:
  template: template-to-call
  requestType: post
  body:
    var1: ${incoming.body.element1}
    var2: "2.0"
  params:
    var3: ${incoming.params.element2}
  result: templateResult
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
