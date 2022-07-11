# Jumps

The execution order of a DSL file can be changed using the `next` keyword, which must contain the name of the step to execute next. 

It is important to note, that without the `next` keyword, steps will still be executed "in order" - so if a step "jumps" to another step, from there on, steps will be executed 
"in order" again (assuming those following steps don't have the `next` keyword). 

Additionally, there is no limit to the amount of times a step can be executed - if a hypothetical `STEP A` "jumps" to another step that somehow leads back to 
the already executed `STEP A`, then it will be executed again, **and the same jump will be done again as well**

If the user wishes to dictate when the processing of a DSL should end, they can use the value `end` for the `next` keyword, which stops the execution of a DSL.

### Example of jump with end
```
first_step:
    call: http.get
    args:
      url: https://example.com/callA
    next: second_step
  
third_step:
    call: http.get
    args:
      url: https://example.com/callB
    next: end

second_step:
    call: http.get
    args:
      url: https://example.com/callC
    next: third_step
```
### Example of jump over step
```
first_step:
    call: http.get
    args:
      url: https://example.com/callA
    next: second_step
  
this_step_is_jumped_over_and_not_executed:
    call: http.get
    args:
      url: https://example.com/callB

second_step:
    call: http.get
    args:
      url: https://example.com/callC
```
### Example of end
```
first_step:
    call: http.get
    args:
      url: https://example.com/callA
  
second_step:
    call: http.get
    args:
      url: https://example.com/callC
      next: end
  
this_step_is_not_executed:
    call: http.get
    args:
      url: https://example.com/callB
```
[Back to Guide](../GUIDE.md#Writing-DSL-files)
