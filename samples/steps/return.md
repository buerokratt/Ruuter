# Respond to request by setting custom cookies

`set-cookies.yaml`

```
# Ruuter service configuration as YAML
- step_1:
    args:
        headers:
            Set-Cookie:
                customCookieName:
                    Value: "customCookieValue"
                    Domain: "https://example.com"
                    Secure: ~
                    HttpOnly: ~
    result: set_custom_cookie

- return_value:
    return ${set_custom_cookie.headers}
```

```
# Matching request in curl
curl https://example.com/set-cookies \
    --cookie 'customCookieName=customCookieValue; Domain=https://example.com; Secure; HttpOnly'
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
