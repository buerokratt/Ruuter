# Template literals

## Add hardcoded value as a prefix to variable

```
- assign:
    personalCode: 1234567890

- return: ${"PNOEE-" + personalCode}
```

```
PNOEE-1234567890
```

## Strings concatenation

```
- assign:
    var1: Büro
    var2: kratt
    var3: www.kratid ee

- return: ${var1 + var2 + " " + var3}
```

```
Bürokratt www.kratid.ee
```
