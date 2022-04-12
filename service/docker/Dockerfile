FROM openjdk:11-jre

COPY --from=build /app/service/build/libs/*.jar /service.jar
ADD service/docker/service-configure /configure

EXPOSE 8080/tcp

ENTRYPOINT ./docker-entrypoint.sh /p8e-cee-api.jar
