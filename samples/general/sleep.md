# Sleep

Every type of step may contain the `sleep` field, which accepts a long value. The value of the field designates an amount of time in `ms`, which when present, 
will make the step *sleep* for that amount of time.

[`sleep.yml`](../../DSL/GET/common/sleep.yml)

```
first_step:
  call: reflect.mock
  args:
    response:
      test: value

this_step_sleeps_for_5_s:
  sleep: 5000
  call: reflect.mock
  args:
    response:
      test: value

third_step:
  call: reflect.mock
  args:
    response:
      test: value
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
