# client/Dockerfile
FROM eclipse-temurin:21-jre-alpine
LABEL authors="ahrem"
COPY target/client-0.0.1.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
