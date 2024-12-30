# React Build
FROM node:20.15 as react-build
WORKDIR /app
RUN ls -la
COPY ./frontend/ .
RUN npm install && npm run build

# Spring Boot Build
FROM eclipse-temurin:22 as spring-build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Final Image
FROM eclipse-temurin:22
COPY --from=spring-build /app/backend/target/*.jar application.jar
COPY --from=react-build /app/frontend/build /app/backend/resources/static
ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8080