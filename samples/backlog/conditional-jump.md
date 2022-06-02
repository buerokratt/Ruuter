# Conditional jump

[https://cloud.google.com/workflows/docs/samples/workflows-step-conditional-weekend](https://cloud.google.com/workflows/docs/samples/workflows-step-conditional-weekend)

```
- step: getCurrentTime
    call: http.get
    args:
      url: https://us-central1-workflowsample.cloudfunctions.net/datetime
    result: currentTime

- step: conditionalSwitch
    switch:
      - condition: ${currentTime.body.dayOfTheWeek == "Friday"}
        next: friday
      - condition: ${currentTime.body.dayOfTheWeek == "Saturday" OR currentTime.body.dayOfTheWeek == "Sunday"}
        next: weekend
    next: workWeek

- step: friday
    return: "It's Friday! Almost the weekend!"

- step: weekend
    return: "It's the weekend!"

- step: workWeek
    return: "It's the work week."
```
