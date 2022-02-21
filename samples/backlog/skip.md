# Skip

Skips the whole step in case `skip: true` is present.

```
- first_step:
    call: http.get
    args:
      url: https://example.com/callA

- second_step:
    skip: true
    call: http.get
    args:
      url: https://example.com/callB

- third_step:
    call: http.get
    args:
      url: https://example.com/callC
```