# Scripting

Many steps allow the DSL writer to utilise scripting. The main purpose of scripting in a DSL is to allow the use of:

* conditional statements
* addition/subtraction and other mathematical operations
* usage of stored variables/step results
* concatenation

Scripting is done in **JavaScript**. Step fields use the following syntax to define where a script begins and ends: `${ script goes between here }`.

***Note: scripting can only be done in certain steps, on certain fields.*** 

### Using a variable from previous steps

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

```
first_step:
    return: ${999 + 1}
    
-----------------------------------------------
Expected result: 1000 
```

### Conditional

```
first_step:
    return: ${"test-string" === "test-string"}
    
-----------------------------------------------
Expected result: true 
```

### Concatenation

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
