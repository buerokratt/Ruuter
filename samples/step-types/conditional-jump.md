# Conditional jump

```
getCurrentTime:
  call: http.get
  args:
    url: https://us-central1-workflowsample.cloudfunctions.net/datetime
  result: currentTime

conditionalSwitch:
  switch:
    - condition: ${currentTime.body.dayOfTheWeek === "Friday"}
      next: friday
    - condition: ${currentTime.body.dayOfTheWeek === "Saturday" || currentTime.body.dayOfTheWeek === "Sunday"}
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
