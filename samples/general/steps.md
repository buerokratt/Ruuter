# Steps

A DSL file consist of different steps. Each step begins with a name, followed by that steps body. The bodies of steps vary, according to the step type - these
types are described in [Step Types](../GUIDE.md#Step-types)
<sub>(it is recommended to read about step types before continuing with "basic functionalities", since many examples use different step types)<sub>

An example of a step:

```
step_name:
    call: http.get
    args:
      url: https://www.example.com/callA
```

Steps are executed in order, from top to bottom:

```
first_step:
    call: http.get
    args:
      url: https://www.example.com/callA

second_step:
    call: http.get
    args:
      url: https://www.example.com/callB

third_step:
    call: http.get
    args:
      url: https://www.example.com/callC
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
