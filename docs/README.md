# National Holidays API Documentation

## List all national holidays this given year
**Endpoint**  
`/calendar/national-holidays`

**Sample query**
```
curl localhost:8080/calendar/national-holidays
```

**Expected outcome**
```
{
    "response": [
        {
            "date": "2024-01-01",
            "name": "uusaasta"
        },
        ...
        {
            "date": "2024-12-26",
            "name": "teine jõulupüha"
        }
    ]
}
```

## Validate if it's a national holiday today
**Endpoint**  
`/calendar/national-holidays/today`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/today
```

**Expected outcome - if current date "2024-03-29"**
```
{
    "response": {
        "date": "2024-03-29",
        "name": "suur reede"
    }
}
```

## Provide the previous national holiday
**Endpoint**  
`/calendar/national-holidays/previous`

**Sample query**
```
curl -X localhost:8080/calendar/national-holidays/previous
```

**Expected outcome**
```
{
    "response": {
        "date": "2024-08-20",
        "name": "taasiseseisvumispäev"
    }
}
```

## Provide the next national holiday
**Endpoint**  
`/calendar/national-holidays/next`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/next
```

**Expected outcome - if current date e.g. "2024-09-24"**
```
{
    "response": {
        "date": "2024-12-24",
        "name": "jõululaupäev"
    }
}
```

## List all national holidays existed this given year until now (including)
**Endpoint**  
`/calendar/national-holidays/ytd`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/ytd
```

**Expected outcome - if current date e.g. "2024-09-24"**
```
{
    "response": [
        {
            "date": "2024-01-01",
            "name": "uusaasta"
        },
        ...
        {
            "date": "2024-08-20",
            "name": "taasiseseisvumispäev"
        }
    ]
}
```

## List all national holidays from today (including) until the end of given year
**Endpoint**  
`/calendar/national-holidays/eoy`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/eoy
```

**Expected outcome - if current date e.g. "2024-09-24"**
```
{
    "response": [
        {
            "date": "2024-12-24",
            "name": "jõululaupäev"
        },
        ...
        {
            "date": "2024-12-26",
            "name": "teine jõulupüha"
        }
    ]
}
```

## List all national holidays based on input text
**Endpoint - accepts holiday name as parameter**  
`/calendar/national-holidays/find/by-name`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/by-name?name=uusaasta
```

**Expected outcome - if text input/passed parameter is "uusaasta"**
```
{
    "response": [
        {
            "date": "2024-01-01",
            "name": "uusaasta"
        }
    ]
}
```

## List all national holidays based on month
**Endpoint - accepts holiday month as parameter**  
`/calendar/national-holidays/find/by-month`

**Sample query**
```
curl /calendar/national-holidays/find/by-month?month=5
```

**Expected outcome - if number input/passed parameter is 5**
```
{
    "response": [
        {
            "date": "2024-05-01",
            "name": "kevadpüha"
        },
        {
            "date": "2024-05-19",
            "name": "nelipühade 1. püha"
        }
    ]
}
```