FROM eclipse-temurin:21-jre-alpine
COPY target/mycall-0.0.1.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
