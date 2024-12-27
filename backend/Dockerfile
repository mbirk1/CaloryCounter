FROM adoptopenjdk:11-jre-hotspot
ARG JAR_FILE=/target/CaloryCounter-0.0.1-SNAPSHOT.jar
RUN echo "Path is:"
RUN pwd
RUN ls -la
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8080