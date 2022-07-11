# Templates

```
- validate_user:
    template: validate-user-session
    result: clientInfo

- initiate_chat:
    template: resql-init-chat
    result: chatInitResponse

- get_chat_default_greeting:
    template: get-chat-default-greeting
    result: chatGreeting

- return_value:
    return:
        headers:
            Set-Cookie:
                activeChatSession:
                    Value: ${chatInitResponse.body.chatID}
                    Domain: https://example.com
                    Secure: ~
                    HttpOnly: ~
        body:
            chatId: ${chatInitResponse.body.chatID}
            response: ${chatGreeting.body}
```
