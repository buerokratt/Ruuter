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

## TLS/SSL  

The `application.yml` must be configured as following:  
```
server:
  port: 8088,8443 ##Configure to reflect the environment
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: 123456 ##Configure to reflect the environment
    key-store-type: PKCS12 
    key-alias: ruuter 
    key-password: 123456 ##Configure to reflect the environment
    enabled: true
```

To generate the key, use folowing command:  
```
keytool -genkeypair -alias ruuter -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
```
Make sure, the password in `application.yml` will match the password you are asked during the key generating  
Make sure, that the generated `keystore.p12` is in `src/main/resources/` before you build the image.  

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
