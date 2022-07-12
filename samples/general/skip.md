# Skip

Every type of step may contain the `skip` field, which accepts a boolean value. If the value is set as `true`, then the step is not executed and is skipped.

```
first_step:
    call: http.get
    args:
      url: https://example.com/callA

this_step_is_skipped:
    skip: true
    call: http.get
    args:
      url: https://example.com/callB

third_step:
    call: http.get
    args:
      url: https://example.com/callC
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
