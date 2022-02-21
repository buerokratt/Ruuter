# Templated service to log error situations

```
- predicates:
    require:
        headers: ${incoming.headers}
        body: ${incoming.body}

- log_error_situation:
    call: http.post
    args:
        url: https://eha/error-by-ruuter
        headers: ${incoming.headers}
        body: ${incoming.body}
```