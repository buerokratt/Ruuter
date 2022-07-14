# Return step

The return step allows to assign return values for the DSL query.

```
return_step:
    return "result"
```

**Mandatory fields:**

* `return` - assigns the value as the final response of the processed DSL. Scripts can be used.

**Optional fields:**

* `status` - if defined, sets the status of the final response, as the assigned status.
* `headers`
    * *..desired header values* - assigns the headers to the response of the processed DSL. Scripts can be used.

***Note - Assigning a return value does not end the processing of the DSL - additionally, an assigned return value/headers can be overwritten by another return
step***

## Examples

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

### Assign a Set-Cookie and a custom header to response

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

### Assign a Set-Cookie and a custom header to response through script

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
