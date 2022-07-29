# Template Step

The template step allows to call out/execute other DSL files.

```
template_step:
  template: template-to-call
  requestType: post
  result: resultVariableName
```

**Mandatory fields:**

* `template` - determines the step type, as well as referencing the DSL file to call out
* `requestType` - since DSL files are categorized by HTTP method types, this indicates which method type DSL to call
  out ([Request types](../GUIDE.md#Request-types))
* `result` - name of the variable to store the response of the template in, for use in other steps

**Optional fields:**

* `args`
    * `body`
        * *..desired body values* - emulating HTTP request body params. Scripts can be used.
    * `query`
        * *..desired param values* - emulating HTTP request query params. Scripts can be used.

#### How responses are stored with the result field

Template results are stored "as-is" into the application context.

## Examples

### Calling out a template

[`template.yml`](../../DSL/GET/steps/template/template.yml)

```
call_template:
  template: return-with-script
  requestType: get
  result: templateResult

return_result:
  return: {templateResult}
```

### Calling out a template with both body and query params

[`template-with-params.yml`](../../DSL/GET/steps/template/template-with-params.yml)

```
call_template:
  template: passing-post-parameters
  requestType: post
  body:
    project: "byk"
    website: "krat.ee"
  query:
    var1: "var"
  result: templateResult

return_result:
  return: ${templateResult}
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
