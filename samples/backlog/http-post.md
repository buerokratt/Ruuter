# HTTP POST request

[https://cloud.google.com/workflows/docs/samples/workflows-http-post](https://cloud.google.com/workflows/docs/samples/workflows-http-post)

```
- get_message:
    call: http.post
    args:
      url: https://example.com/endpoint
      body:
        some_val: "Hello World"
        another_val: 123
    result: the_message

- return_value:
    return: ${the_message.body}
```