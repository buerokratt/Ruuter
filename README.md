> PS! Ruuter of this repo is currently in a state of "functionally working, but **not ready for use in production**"!
>
> Although we are building new functionalities of Bürokratt based on Ruuter 2.0, we aren't using them in production until penetration tests have been run on Ruuter. This is planned for Q1 in 2023.
>
> There are currently (some serious) security issues that we are aware of.

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
