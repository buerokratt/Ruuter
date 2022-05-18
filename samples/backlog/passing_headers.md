# Passing custom headers with a request

*The same goes for GET and other request types as well.*

```
# Ruuter service configuration as YAML
call: http.post
args:
    url: https://example.com/post-endpoint
    headers:
        X-Custom-Header: "Some custom header value"
        X-Special-Header: "Special header"
```

```
# Matching request in curl
curl https://example.com/post-endpoint \
    -H 'X-Custom-Header: Some custom header value' \
    -H 'X-Special-Header: Special header'
```
