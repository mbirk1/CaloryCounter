# Spring Boot Build
FROM eclipse-temurin:22 as spring-build
COPY . .
RUN mvn clean package -DskipTests

# Final Image
FROM eclipse-temurin:22
COPY --from=spring-build /target/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8080