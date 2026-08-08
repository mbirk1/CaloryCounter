# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------------
# Builds the Spring Boot jar. The frontend-maven-plugin bound to this pom
# (see backend/pom.xml) downloads its own pinned Node/npm and builds the
# Angular app as part of `mvn package`, then the resources plugin copies the
# compiled static assets into the jar - so this single stage produces the
# fully self-contained artifact.
FROM maven:3.9.9-eclipse-temurin-22 AS build
WORKDIR /workspace
COPY backend/ backend/
COPY frontend/ frontend/
WORKDIR /workspace/backend
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests

# ---- Runtime stage --------------------------------------------------------
# JRE-only base (no JDK/Maven) keeps the shipped image lean.
FROM eclipse-temurin:22-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/backend/target/calory-0.0.1-SNAPSHOT.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
