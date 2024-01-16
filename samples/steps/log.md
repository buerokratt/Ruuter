# Log step

Allows logging specific values for debug reasons


```
log_step_1:
    log: "result"

assign_step:
  assign:
    testkey: "testvalue"

log_step_2:
    log: ${testkey}
```
