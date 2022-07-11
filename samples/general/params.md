# Passing parameters to DSL

Often it's necessary to get either the body or parameters of the incoming request, to use them in the DSL. Ruuter stores these variables and allows their use 
through the following keywords:
* query parameters: `incoming.params`
* request body: `incoming.body`

### Example of using params

```
https://ruuter/passing-get-parameters?passedVariable=passedValue
```
```
passing-get-parameters.yml
```
```
first_step:
    return: ${incoming.params.passedVariable}
    
--------------------------------------------
Expected result: "passedValue" 
```

### Example of using body

```
curl https://ruuter/passing-post-parameters \
    -H 'Content-Type: application/json' \
    -d '{"project": "Bürokratt", "website": "www.kratid.ee"}'
```
```
passing-post-parameters.yml
```
```        
first_step:
    return: ${incoming.body.project} - ${incoming.body.website}
    
--------------------------------------------
Expected result: "Bürokratt - www.kratid.ee" 
```


[Back to Guide](../GUIDE.md#Writing-DSL-files)
