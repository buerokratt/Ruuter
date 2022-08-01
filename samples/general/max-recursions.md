# Max recursions

The max recursions is for executing step a specific number of times and avoiding infinite loops. It can be specified for each step. When a step has reached its maximum recursions,
Ruuter will continue from a next step that was not in a loop. Max recursions can also be defined in an application.yml, but that will apply to all the steps.
Step specific max recursions will override the global value in an `application.yml` file only if step specific is smaller than the global value.

### Examples of using step specific max recursions

#### First Example

In this case get_step will be executed 5 times and after that Ruuter will move on to the next step, which is return_step.
```
get_step:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: "Hello World"
      another_val: 123
  maxRecursions: 5
  next: get_step
  result: the_message

return_step
  return: ${the_message}
```

#### Second example

In this case get_step has again max recursions 5. The difference is that now there are 3 steps in a loop (assign_step, get_step and post_step).
These 3 steps will be executed 5 times and when it is done, next step outside the loop will be executed, which in this case is return_step.
```
assign_step:
  assign:
    variableName: "variable value"
  next: get_step

get_step:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: "Hello World"
      another_val: 123
  maxRecursions: 5
  next: post_step
  result: the_message
  
post_step:
  call: http.post
  args:
    url: https://www.example-url.com
    body:
      test: "param"
  next: assign_step
  result: responseVariable
  
return_step
  return: ${the_message}
```
