FROM adoptopenjdk:11-jre-hotspot
ARG JAR_FILE=/target/CaloryCounter-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} CaloryCounter-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "CaloryCounter-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080