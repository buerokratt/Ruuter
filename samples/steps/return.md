# Return step

### Assign return value
```
return_value:
    return "result"
```

### Assign return value with script
```
assign_step:
    assign:
        variable: "result"

return_value:
    return ${variable}
```

### Set custom cookies to response
`set-cookies.yaml`
```
return_value:
    headers:
        Set-Cookie:
            customCookieName: "customCookieValue"
            Domain: "https://example.com"
            Secure: false
            HttpOnly: true
        custom-header: "customValue"
    return "result"
```

### Set custom cookies to response with script
`set-cookies-with-script.yaml`
```
assign_step:
    assign:
        setCookie:
            customCookieName: "customCookieValue"
            Domain: "https://example.com"
            Secure: false
            HttpOnly: true
        customHeader: "customValue"

return_value:
    headers:
        Set-Cookie: ${setCookie}
        custom-header: ${customHeader}
    return "result"
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
