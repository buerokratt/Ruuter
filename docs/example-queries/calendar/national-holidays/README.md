# National Holidays API Documentation

## List all national holidays this given year
**Endpoint**  
`/calendar/national-holidays/national-holidays`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/national-holidays
```

**Expected outcome**
```
{
  "result": "Kõik pühad sellel aastal on:\n * 2024-01-01\nuusaasta\n\n * 2024-02-24\niseseisvuspäev, Eesti Vabariigi aastapäev\n\n * 2024-03-29\nsuur reede\n\n * 2024-03-31\nülestõusmispühade 1. püha\n\n * 2024-05-01\nkevadpüha\n\n * 2024-05-19\nnelipühade 1. püha\n\n * 2024-06-23\nvõidupüha\n\n * 2024-06-24\njaanipäev\n\n * 2024-08-20\ntaasiseseisvumispäev\n\n * 2024-12-24\njõululaupäev\n\n * 2024-12-25\nesimene jõulupüha\n\n * 2024-12-26\nteine jõulupüha\n\n"
}
```

## Validate if it's a national holiday today
**Endpoint**  
`/calendar/national-holidays/today`

**Sample query**
```
curl localhost:8080/calendar/national-holidays/today
```

**Expected outcome - if current date "2024-03-29" **
```
{
    "response": {
        "result": "Täna on suur reede"
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
        "result": "Viimane riigipüha oli taasiseseisvumispäev - 2024-08-20"
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
        "result": "Tulev riigipüha on jõululaupäev - 2024-12-24"
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
    "result": "Kõik pühad aasta algusest kuni tänaseni:\n* 2024-01-01\nuusaasta\n\n* 2024-02-24\niseseisvuspäev, Eesti Vabariigi aastapäev\n\n* 2024-03-29\nsuur reede\n\n* 2024-03-31\nülestõusmispühade 1. püha\n\n* 2024-05-01\nkevadpüha\n\n* 2024-05-19\nnelipühade 1. püha\n\n* 2024-06-23\nvõidupüha\n\n* 2024-06-24\njaanipäev\n\n* 2024-08-20\ntaasiseseisvumispäev\n"
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
  "result": "Kõik riigipühad alates tänsest kuni aasta lõpuni on: \n*2024-12-24\n jõululaupäev\n\n *2024-12-25\n esimene jõulupüha\n\n *2024-12-26\n teine jõulupüha\n\n"
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
**Service will default to "uusaasta" if no parameter given**



```
{
    "response": {
        "result": "Uusaasta on 2024-01-01"
    }
}
```

**Service will return "Ei leia sellist riigipüha" if no value found**
```
{
    "response": {
        "result": "Ei leia sellist riigipüha"
    }
}
```

## List all national holidays based on month
**Endpoint - accepts holiday month as parameter**  
`/calendar/national-holidays/find/by-month`

**Sample query**
```
curl /calendar/national-holidays/find/by-month?month=6
```

**Expected outcome - if number input/passed parameter is 6**
```
{
  "result": "Kõik riigipühad antud kuus on: \n*2024-06-23\n võidupüha\n\n*2024-06-24\n jaanipäev\n\n"
}
```

**Service defaults to current month if no value given - if current month e.g. "08"**
```
{
  "result": "Kõik riigipühad antud kuus on: \n*2024-08-20\n taasiseseisvumispäev\n\n"
}
```

**If no holiday found**
```
{
    "response": {
        "result": "Ei leia riigipühi antud kuus"
    }
}
```