FROM eclipse-temurin:21-jre-alpine
COPY target/mycall-0.0.1.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
