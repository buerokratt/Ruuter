# Conditional jump Step

The conditional jump step allows to jump to other steps, based on certain conditions.

```
conditional_step:
  switch:
    - condition: ${ ...condition }
      next: step_name_1
    - condition: ${ ...condition }
      next: step_name_2
  next: step_name_3
```

**Mandatory fields:**

* `switch` - determines the step type
    * `- condition` - script containing condition
    * `next` - name of the step to jump to, if condition is true

**Optional fields:**

* `next` - if no condition is true, the value of this will be used to jump to the next step

## Examples

### Using conditional jump to jump to other steps

[`conditional-jump.yml`](../../DSL/GET/steps/switch/conditional-jump.yml)

```
getCurrentTime:
  call: http.get
  args:
    url: https://us-central1-workflowsample.cloudfunctions.net/datetime
  result: currentTime

conditionalSwitch:
  switch:
    - condition: ${currentTime.response.body.dayOfTheWeek === "Friday"}
      next: friday
    - condition: ${currentTime.response.body.dayOfTheWeek === "Saturday" || currentTime.response.body.dayOfTheWeek === "Sunday"}
      next: weekend
  next: workWeek

friday:
  return: "It's Friday! Almost the weekend!"
  next: end

weekend:
  return: "It's the weekend!"
  next: end

workWeek:
  return: "It's the work week."
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
