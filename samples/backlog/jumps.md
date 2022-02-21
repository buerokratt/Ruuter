# Jumps

[https://cloud.google.com/workflows/docs/reference/syntax/jumps](https://cloud.google.com/workflows/docs/reference/syntax/jumps)


```
- first_step:
    call: http.get
    args:
      url: https://example.com/callA
    next: second_step
  
- third_step:
    call: http.get
    args:
      url: https://example.com/callB
    next: end

- second_step:
    call: http.get
    args:
      url: https://example.com/callC
    next: third_step
```