# Ruuter
- Java 17, Gradle

## Configuration

### External forwarding
If the following values are defined, then every incoming request is forwarded to the defined ednpoint.
* method - accepted values: `GET`, `POST`
* paramsToPass.get - whether to forward query params from incoming request
* paramsToPass.post - whether to forward body from incoming request
* paramsToPass.headers - whether to forward headers from incoming request
* proceedPredicate.httpStatusCode - defines the accepted http statuscodes for proceeding with configuration processing. If the external forwarding request
 receives a response not included in the list, then the request is not processed.
  * values can be defined as single status codes: `200`, `201`, `202`...
  * values can be defined as a range, using `..` special syntax: `200..202`
```
incomingRequests:
    externalForwarding:
        method: POST
        endpoint: "https://turvis/ruuter-incoming"
        paramsToPass:
            GET: true
            POST: false
            headers: true
        proceedPredicate:
            httpStatusCode: [ 200..202, 204 ]
```

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


## License

See licence [here](LICENSE).
