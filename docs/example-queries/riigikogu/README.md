# Most Recent Voting Results API Documentation

## Fetch 5 Most Recent Voting Results

**Endpoint**
```
/riigikogu/most-recent-votings
```

***Descriotion**
This service retrieves information about the 5 most recent public voting results from the Riigikogu API. The results include detailed information such as attendance, voting counts, and associated draft titles.

**Sample query**
```
curl localhost:8080/services/most-recent-votings
```

**Expected outcome**
```
{
    "response": [
        {
            "title": "Perehüvitiste seaduse muutmise seadus",
            "present": 81,
            "absent": 20,
            "inFavor": 46,
            "against": 20,
            "neutral": 0,
            "abstained": 15
        },
        {
            "title": "Vabariigi Valitsuse seaduse muutmise seadus",
            "present": 84,
            "absent": 17,
            "inFavor": 53,
            "against": 25,
            "neutral": 0,
            "abstained": 6
        },
        ...
    ]
}

```