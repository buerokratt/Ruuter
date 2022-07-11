# HTTP GET Step

### Standard GET request

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: "Hello World"
      another_val: 123
  result: the_message
```

### GET request with custom header and without query parameters

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message
```

### GET request with variables

```
assign_values:
    assign:
        stringValue: "Bürokratt"
        integerValue: 2021

get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    query:
      some_val: ${stringValue}
      another_val: ${integerValue}
  result: the_message
```

### Using GET request response

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message

return_value:
  return: ${the_message.response}
```

### Using GET request parameters

```
get_message:
  call: http.get
  args:
    url: https://example.com/endpoint
    headers:
      Content-Type: "text/plain"
  result: the_message

return_value:
  return: ${the_message.request}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
