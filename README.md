> https://github.com/buerokratt/Ruuter/releases/tag/2.0.1-beta is the latest `pentested` version of Ruuter
>
> The latest state of Ruuter can be found from https://github.com/buerokratt/Ruuter/tree/dev
>
> The next major pentest is planned for `Q1 2024`, but there haven't been many major changes since the last pentest.

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
