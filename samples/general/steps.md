# Steps

A DSL file consist of different steps. Each step begins with a name, followed by that steps body. The bodies of steps vary, according to the step type - these
types are described in [Step Types](../GUIDE.md#Step-types)

*<sub>Note: it is recommended to read about [`return`](../steps/return.md), [`mock`](../steps/mock.md) and [`assign`](../steps/assign-variables.md) steps before continuing with "other chapters", since most examples are given using these step types<sub>*

An example of a step:

```
step_name:
    call: http.get
    args:
      url: https://www.example.com/callA
```

Steps are executed in order, from top to bottom:

[`steps.yml`](../../DSL/GET/order/steps.yml)

```
first_step:
  call: reflect.mock
  args:
    response:
      test: value

second_step:
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
