### Using temporary authorization

For scripts that have to send Ruuter without using TIM/JWT
authorization, there is a possibility to use one-time tokens
that can be asked from Resql.

#### Generate nonce

Resql:

```
GET /training/get-new-nonce
```

returns a JSON object with one field `nonce`,
this value can be used for guard-controlled Ruuter
endpoints by putting it into header value 
`x-ruuter-nonce` or query parameter `ruuter-nonce`.


##### Example:

script -> Resql
```
GET /training/get-new-nonce
```

