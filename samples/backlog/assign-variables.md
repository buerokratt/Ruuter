# #1

```
- step_1:
    assign:
        - stringValue: "Bürokratt"
        - integerValue: 2021
        
- step_2:
    assign:
        - doubleNumber: 2.0

return ${stringValue + " v" + doubleNumber + " since " + integerValue}
```

```
Bürokratt v2.0 since 2021
```

# #2

```
- step_1:
    assign:
        - stringValue: "Bürokratt"
        - integerValue: 2021
        
- step_2:
    assign:
        - doubleNumber: 2.0

- step_3:
    assign:
        - concatenated: " v" + ${doubleNumber + " since " + integerValue}

return ${stringValue} + ${concatenated}
```

```
Bürokratt v2.0 since 2021
```

# #3

```
https://ruuter/passing-get-parameters?passedVariable=passedValue
```

`passing-get-parameters.yaml`

```
- first_step:
    call: http.get
    args:
        url: https://ruuter/custom-get-endpoint
        query:
            passingOn: ${incoming.get.passedVariable}
```

```
curl 'https://ruuter/custom-get-endpoint?passingOn=passedValue'
```

# #4

```
curl https://ruuter/passing-post-parameters \
    -H 'Content-Type: application/json' \
    -d '{"project": "Bürokratt", "website": "www.kratid.ee"}'

```

`passing-post-parameters.yaml`

```
- first_step:
    call: http.post
    args:
        url: https://example.com/post-endpoint
        body:
            project: ${incoming.post.incomingProject}
            website: ${incoming.post.incomingWebsite}
```

```
# Matching request in curl
curl https://example.com/post-endpoint \
    -H 'Content-Type: application/json' \
    -d '{"project": "Bürokratt", "website": "www.kratid.ee"}'
```