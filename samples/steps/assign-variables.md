# Assign Step

The assign step allows to assign different types of variables.

```
assign_step:
  assign:
    variableName: "variable value"
```

**Mandatory fields:**

* `assign` - determines the step type
    * *...desired variables* - scripts can be used for variable values

## Examples

### Assigning variables and using them

```
step_1:
  assign:
    stringValue: "Bürokratt"
    integerValue: 2021

step_2:
  assign:
    doubleNumber: "2.0"

step_3:
  assign:
    concatenated: v${doubleNumber + " since " + integerValue}

step_4:
  return: ${stringValue} ${concatenated}

--------------------------------------------
Expected result: "Bürokratt v2.0 since 2021" 
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
