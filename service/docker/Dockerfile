FROM gradle:7.3.3-jdk11 AS build
RUN gradle --version && java -version
WORKDIR /app

ARG NEXUS_USER
ARG NEXUS_PASS

# Only copy dependency-related files
COPY build.gradle.kts gradle.properties settings.gradle.kts buildSrc/build.gradle.kts /app/

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build -PnexusPass=$NEXUS_PASS -PnexusUser=$NEXUS_USER --no-daemon > /dev/null 2>&1 || true

# Copy all files
COPY service /app/service/
COPY models /app/models/
COPY buildSrc /app/buildSrc/
COPY .git /app/.git/
COPY detekt.yml /app/
COPY build.gradle.kts gradle.properties settings.gradle.kts /app/
COPY CHANGELOG.md CODE_OF_CONDUCT.md CONTRIBUTING.md LICENSE README.md /app/

# Do the actual build
RUN gradle clean build -PnexusPass=$NEXUS_PASS -PnexusUser=$NEXUS_USER --no-daemon

FROM openjdk:11-jre

ADD service/build/libs/*.jar /p8e-cee-api.jar
ADD service/docker/service-configure /configure

EXPOSE 8080/tcp

ENTRYPOINT /configure /p8e-cee-api.jar
