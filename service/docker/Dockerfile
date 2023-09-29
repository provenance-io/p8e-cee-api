FROM gradle:7.6.2-jdk17-alpine AS build
RUN gradle --version && java -version
WORKDIR /app

# Only copy dependency-related files
COPY build.gradle.kts gradle.properties settings.gradle.kts buildSrc/build.gradle.kts /app/

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

# Copy all files
COPY service /app/service/
COPY models /app/models/
COPY buildSrc /app/buildSrc/
COPY .git /app/.git/
COPY detekt.yml /app/
COPY build.gradle.kts gradle.properties settings.gradle.kts /app/
COPY CODE_OF_CONDUCT.md LICENSE README.md /app/

# Do the actual build
RUN gradle clean build --no-daemon

FROM azul/zulu-openjdk:17-jre-latest

ADD service/build/libs/*.jar /p8e-cee-api.jar
ADD service/docker/service-configure /configure

EXPOSE 8080/tcp

ENTRYPOINT /configure /p8e-cee-api.jar
