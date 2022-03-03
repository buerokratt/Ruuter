# Sample service

```
- step_1:
    call: http.post
    args:
      url: https://example.com/A
      body:
        some_val: "Hello World"
        another_val: 123
    result: the_message

- step_2:
    skip: true
    call: http.get
    args:
      url: https://example.com/B

- step_3:
    call: http.get
    args:
      url: https://example.com/C
    result: the_message

- return_value:
    return: ${the_message.body}
```

## Output in logs

Assuming that
1) external validation of input (Ruuter´s built-in functionality) is enabled (see `incoming.validation` in logs);
2) showing request content in logs is disabled;
3) showing response content in logs is disabled.

```
timestamp      component_version log_level request_author_IP   request_type         forwarded_id       request_id       request_to             request_content response_content    response_code   response_in_ms
1646220981982  2.0.1             INFO      0.0.0.0             incoming.request     -                  NBZKMWMMMwm72Mvm -                      -               -                   200             1
1646220981983  2.0.1             INFO      0.0.0.0             incoming.validation  NBZKMWMMMwm72Mvm   pLN3dUUNpmQUMLFr https://turvis         -               -                   200             4
1646220981987  2.0.1             INFO      0.0.0.0             http.post            NBZKMWMMMwm72Mvm   VXpQqgQ3GzzS3cXs https://example.com/A  -               -                   200             6
1646220981993  2.0.1             INFO      0.0.0.0             skip                 NBZKMWMMMwm72Mvm   -                -                      -               -                   -               1
1646220981994  2.0.1             ERROR     0.0.0.0             http.get             NBZKMWMMMwm72Mvm   zdpLqW7fRFAb98G9 https://example.com/C  -               -                   418             2
1646220981996  2.0.1             INFO      0.0.0.0             incoming.response    NBZKMWMMMwm72Mvm   -                -                      -               -                   200             -
```