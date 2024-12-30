FROM eclipse-temurin:21
ARG JAR_FILE=/target/calory-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8080