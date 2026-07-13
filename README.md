# Ruuter
- Java 17, Gradle

## Guide
See guide [here](./samples/GUIDE.md)

## Configuration
See configuration [here](./samples/CONFIGURATION.md)

## Docker

To run the application using Docker, run:

```
docker-compose up -d
```

## Testing

To launch the application's tests, run:

```
gradlew test
```

This requires a local JDK matching the project's version (see `Dockerfile`). If your local JDK doesn't
match, run the tests in Docker instead - no local JDK required:

```
docker build -f Dockerfile.test .
```

This builds and runs the full test suite inside a container matching the project's JDK version, and
fails the build (non-zero exit code) if any test fails. It doesn't produce a runnable image - it's a
test gate, not a deployment artifact.


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
