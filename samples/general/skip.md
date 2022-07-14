# Skip

Every type of step may contain the `skip` field, which accepts a boolean value. If the value is set as `true`, then the step is not executed and is skipped.

[`skip.yml`](../../DSL/GET/common/skip.yml)

```
first_step:
  call: reflect.mock
  args:
    response:
      test: value

skipped_step:
  skip: true
  call: reflect.mock
  args:
    response:
      test: value
  next: end

second_step:
  call: reflect.mock
  args:
    response:
      test: value
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
