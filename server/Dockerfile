# server/Dockerfile
FROM eclipse-temurin:21-jre-alpine
LABEL authors="ahrem"
COPY target/server-0.0.1.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar", "--server.port=8081"]
