# Sleep

Every type of step may contain the `sleep` field, which accepts a long value. The value of the field designates an amount of time in `ms`, which when present, 
will make the step *sleep* for that amount of time.

```
first_step:
    call: http.get
    args:
      url: https://example.com/callA

this_step_sleeps_for_5_s:
    sleep: 5000
    call: http.get
    args:
      url: https://example.com/callB

third_step:
    call: http.get
    args:
      url: https://example.com/callC
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
