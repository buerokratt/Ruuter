```
- predicates:
    require:
        body: ${incoming.body}

- initiate_chat:
    call: http.post
    args:
        url: https://resql/init-chat
        body:
            message: ${body.message}
            clientFullName: ${clientInfo.body.fname + " " + clientInfo.body.sname}
            clientPersonalCode: ${clientInfo.body.personalCode
    result: chatInitResponse
```