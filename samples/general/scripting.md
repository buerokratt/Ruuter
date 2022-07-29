# Scripting

Many steps allow the DSL writer to utilise scripting. The main purpose of scripting in a DSL is to allow the use of:

* conditional statements
* addition/subtraction and other mathematical operations
* usage of stored variables/step results
* concatenation

Scripting is done in **JavaScript**. Step fields use the following syntax to define where a script begins and ends: `${ script goes between here }`.

***Note: scripting can only be done in certain steps, on certain fields - these fields are indicated in individual step type guides under [Step Types](../GUIDE.md#Step-types).*** 

### Using a variable from previous steps

[`use-variable.yml`](../../DSL/GET/scripting/use-variable.yml)

```
first_step:
  assign:
    variable: "myVariable"

second_step:
  return: ${variable}
    
-----------------------------------------------
Expected result: "myVariable" 
```

### Computation

[`computation.yml`](../../DSL/GET/scripting/computation.yml)

```
first_step:
  return: ${999 + 1}
    
-----------------------------------------------
Expected result: 1000 
```

### Conditional

[`conditional.yml`](../../DSL/GET/scripting/conditional.yml)

```
first_step:
  return: ${"test-string" === "test-string"}
    
-----------------------------------------------
Expected result: true 
```

### Concatenation

[`concatenation-1.yml`](../../DSL/GET/scripting/concatenation-1.yml)

```
first_step:
  assign:
    variable1:
      value: "test"
    variable2: "string"

second_step:
  return: ${variable1.value + " " + variable2}
    
-----------------------------------------------
Expected result: "test string" 
```

[`concatenation-2.yml`](../../DSL/GET/scripting/concatenation-2.yml)

```
first_step:
  assign:
    variable1:
      value: "test"
    variable2: "string"

second_step:
  return: ${variable1.value} ${variable2}
    
-----------------------------------------------
Expected result: "test string" 
```

[`concatenation-3.yml`](../../DSL/GET/scripting/concatenation-3.yml)

```
first_step:
  assign:
    variable: "test"

second_step:
  return: PREFIX-${variable}
    
-----------------------------------------------
Expected result: "PREFIX-test" 
```

[Back to Guide](../GUIDE.md#Writing-DSL-files)
