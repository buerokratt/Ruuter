FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle .
COPY src src
COPY .env .env

RUN chmod 754 ./gradlew
RUN ./gradlew -Pprod clean bootJar
RUN mkdir -p build/libs && (cd build/libs; jar -xf *.jar)

FROM eclipse-temurin:21-jdk-alpine
VOLUME /build/tmp

ARG DEPENDENCY=/workspace/app/build/libs
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Spring Boot 4 removed the "classic" bootJar loader, so the fat jar's SPI registration now points
# at org.springframework.boot.loader.nio.file.NestedFileSystemProvider - a class that lives outside
# BOOT-INF (at the jar root) and is never copied into this exploded, flat-classpath layout. Left in
# place, the first NIO FileSystemProvider lookup (e.g. from GraalJS) throws ServiceConfigurationError:
# "Provider ...NestedFileSystemProvider not found". Nested-jar support isn't needed here since
# BOOT-INF/lib is already unpacked to plain jars, so just drop the dangling registration.
RUN rm -f /app/META-INF/services/java.nio.file.spi.FileSystemProvider

ENV application.config-path=/DSL

COPY entrypoint.sh ./entrypoint.sh

COPY .env /app/.env
RUN echo BUILDTIME=`date +%s%N | cut -b1-13` >> /app/.env

RUN adduser -D ruuter
RUN mkdir logs
RUN mkdir DSL
RUN chown ruuter:ruuter /logs
RUN chown -R ruuter:ruuter /app
RUN chown -R ruuter:ruuter /DSL
USER ruuter


# ENTRYPOINT ["java","-cp","app:app/lib/*","ee.buerokratt.ruuter.RuuterApplication"]

ENTRYPOINT ["./entrypoint.sh"]
CMD ["java", "-Djavax.net.ssl.trustStore=/app/my-keystore.jks", "-Djavax.net.ssl.trustStorePassword=mypassword", "-cp", "app:app/lib/*", "ee.buerokratt.ruuter.RuuterApplication"]
