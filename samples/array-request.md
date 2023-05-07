# Array request

Here is an example request containing array as part of request body.

This request queries `stat.ee` endpoint `RV021`, which returns population statistics.

```
post_step:
  call: http.post
  args:
    url: https://andmed.stat.ee/api/v1/et/stat/RV021
    body:
      query:
        - code: Sugu
          selection:
            filter: item
            values:
              - 1
              - 2
        - code: Aasta
          selection:
            filter: item
            values:
              - 2021
              - 2022
      response:
        format: json-stat2
  result: res

return_value:
  return: ${res.response.body}
```
