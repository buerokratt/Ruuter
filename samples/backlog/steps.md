# Steps

[https://cloud.google.com/workflows/docs/reference/syntax/steps](https://cloud.google.com/workflows/docs/reference/syntax/steps)

```
- first_step:
    call: http.get
    args:
      url: https://www.example.com/callA

- second_step:
    call: http.get
    args:
      url: https://www.example.com/callB

- third_step:
    call: http.get
    args:
      url: https://www.example.com/callC
```