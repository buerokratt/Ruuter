# Jumps

The execution order of a DSL file can be changed using the `next` field, which must contain the name of the step to execute next, as its value.

It is important to note, that without the `next` field, steps will still be executed "in order" - so if a step "jumps" to another step, from there on, steps
will be executed
"in order" again (assuming those following steps don't have the `next` field).

Additionally, there is no limit to the amount of times a step can be executed - if a hypothetical `STEP A` "jumps" to another step that somehow leads back to
the already executed `STEP A`, then `STEP A` will be executed again, **and the same jump will be done again as well**.

If the user wishes to determine when the processing of a DSL should end, they can use the value `end` for the `next` field, which stops the execution of a DSL.

### Example of jump with end

[`jump-with-end.yml`](../../DSL/GET/order/jump-with-end.yml)

```
first_step:
  call: reflect.mock
  args:
    response:
      test: value
  next: second_step

third_step:
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
  next: third_step
```

### Example of jump over step

[`jump-over-step.yml`](../../DSL/GET/order/jump-over-step.yml)

```
first_step:
  call: reflect.mock
  args:
    response:
      test: value
  next: second_step

this_step_is_jumped_over_and_not_executed:
  call: http.get
  args:
    url: https://example.com/callB

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

### Example of end

[`end-execution.yml`](../../DSL/GET/order/end-execution.yml)

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
  next: end

this_step_is_not_executed:
  call: http.get
  args:
    url: https://example.com/callB
```

### Example of an *ad hoc* loop

It is possible to use jumping for an *ad hoc* looping. 
This method does not keep stack and the end condition has to be 
specifically added.

```
initialize_array:
  assign:
    array: []
    index: 0

process_next_index:
  switch:
    - condition: ${index < 5 && index === 0}
      next: loop_first_index
    - condition: ${index < 5 && index > 0}
      next: loop_index  
  next: return_array

loop_first_index:
  assign:
    array: ${[array, index]}
    index: ${index + 1}
  next: process_next_index

loop_index:
  assign:
    array: ${[...array, index]}
    index: ${index + 1}
  next: process_next_index 

return_array:
  return: ${array.splice(1 , array.length - 1)}
  next: end
```


[Back to Guide](../GUIDE.md#Writing-DSL-files)
