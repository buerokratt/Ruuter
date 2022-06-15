Use reflected requests to imitate to-be API calls that do not yet exist.

`reflected-request.md`

```
- step_1:
    call: reflect.mock
    args:
        request:
            some: "request"
        response:
            project: "Bürokratt"
            website: "www.kratid.ee"
    result: reflected_request

- step_2:
    result: ${reflected_request.body.project EQ "Bürokratt"}

return: ${reflected_request.body.project}
```

```
curl https://ruuter/reflected-request

{
	"request": {
		"some": "request"
	},
	"response": {
		"project": "Bürokratt",
		"website": "www.kratid.ee"
	}
}
```
