# Ruuter
- Java 17, Gradle

## Guide
See guide [here](./samples/GUIDE.md)

## Configuration
See configuration [here](./samples/CONFIGURATION.md)

## Docker

To run the application using docker run:

```
docker-compose up -d
```

## Testing

To launch the application's tests, run:

```
gradlew test
```


## Building for production

### Packaging as jar

To build the final jar run:

```
gradlew -Pprod clean bootJar
```

To ensure everything worked, run:

```
java -jar build/libs/*.jar
```

### Packaging as war

To package the application as a war in order to deploy it to an application server, run:

```
gradlew -Pprod -Pwar clean bootWar
```

## Configuration
### Final Response status code

Final response status code can be defined in application.yml. If defined, all final responses by Ruuter will respond with this status code no matter what.
There are two options. One is for DSLs that have a response and the other one is for DSLs that do not have a response.

The business value lies in not allowing perpetrators to fish for exceptions, weaknesses, etc in back-end systems via requests from public network.

Nevertheless, applications (including Ruuter's) log files contain actual HTTP response codes, being the appropriate source for debugging, alerting, etc.
```
finalResponse:
  dslWithResponseHttpStatusCode: 200
  dslWithoutResponseHttpStatusCode: 300
```

### Default service

Default service is a service file that will be executed when the request status code is not within the allowlist of HTTP response codes.

Default service can be defined in the `application.yml` file by the name of `defaultServiceInCaseOfException`. The service itself is yet another service in `/dsl/POST` folder. Both `body` and `query` parameters are optional and covered here as an example.

```
defaultServiceInCaseOfException:
    service: default-service
    body:
      someVal: "Hello World"
    query:
      anotherVal: 123
```


### Http request default headers

There is an example for POST requests. To add default headers to other requests in application.yml, define it in application.yml, also in
ApplicationProperties class the same way as HttpPost is defined and use addHeaders method from HttpQueryArgs to add these default headers to the request.


## License

See licence [here](LICENSE).
