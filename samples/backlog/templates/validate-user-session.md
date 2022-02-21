# Templated service of validating user's JWT

```
- predicates:
    require:
        headers: ${incoming.headers}

- validate_jwt:
    call: http.get
    args:
        url: https://tim/jwt-validate
        headers: ${headers}
    result: jwt_content

- conditional:
    switch:
      - condition: ${jwt_content.headers.code != 200}
        template: log_error
        next: end
        
    next: jwt_ok

- jwt_ok:
    return: ${jwt_content.body}
```