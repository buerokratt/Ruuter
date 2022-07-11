# HTTP POST Step

```
get_message:
  call: http.post
  args:
    url: https://example.com/endpoint
    body:
      some_val: "Hello World"
      another_val: 123
  result: the_message

return_value:
  return: ${the_message.body}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
