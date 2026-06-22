# Passing parameters to DSL

Often it's necessary to get either the body or parameters of the incoming request, to use them in the DSL. Ruuter stores these variables and allows their use
through the following keywords:

* query parameters: `incoming.params`
* request body: `incoming.body`

### Example of using params

```
https://ruuter/scripting/passing-get-parameters?passedVariable=passedValue
```

[`passing-get-parameters.yml`](../../DSL/GET/scripting/passing-get-parameters.yml)

```
first_step:
  return: ${incoming.params.passedVariable}
    
--------------------------------------------
Expected result: "passedValue" 
```

#### Path parameters

Endpoints support path parameters, these values are inserted into
incoming parameter list `incoming.params` under the key pathParams
as a list in the same order:

##### Example:

```
https://ruuter/scripting/passing-path-parameters/value3/value1/value2
```

```
first_step:
  return: ${incoming.params.pathParams}
    
--------------------------------------------
Expected result: ["value3","value1","value2"] 
```

### Example of using body

```
curl https://ruuter/scripting/passing-post-parameters \
    -H 'Content-Type: application/json' \
    -d '{"project": "Bürokratt", "website": "www.kratid.ee"}'
```

[`passing-post-parameters.yml`](../../DSL/POST/scripting/passing-post-parameters.yml)

```        
first_step:
  return: ${incoming.body.project} - ${incoming.body.website}
    
--------------------------------------------
Expected result: "Bürokratt - www.kratid.ee" 
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
