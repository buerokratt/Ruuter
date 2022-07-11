# Sleep

A step can be made to sleep for input amount of time in `ms`.

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
