# ========================================================
# Curriculum Intelligence System — Backend Dockerfile
# Multi-stage production build (Maven Temurin 21 -> Eclipse Temurin 21 JRE)
# ========================================================

# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile source code and package JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for container health checks
RUN apk add --no-cache curl

# Copy compiled JAR artifact
COPY --from=builder /app/target/curriculum-intelligence-backend-1.0.0-SNAPSHOT.jar app.jar

# Expose Spring Boot application port
EXPOSE 8081

# JVM flags optimized for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
