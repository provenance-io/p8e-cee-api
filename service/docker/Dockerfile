FROM openjdk:11-jre

ADD build/libs/service*.jar /p8e-cee-api.jar
ADD docker/docker-entrypoint.sh /docker-entrypoint.sh

RUN apt-get update && apt-get install -y procps

EXPOSE 8080/tcp

ENTRYPOINT ./docker-entrypoint.sh /p8e-cee-api.jar
