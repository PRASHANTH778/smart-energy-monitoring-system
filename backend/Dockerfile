# ===================================================================
# Dockerfile for Spring Boot Backend on Render / Cloud
# ===================================================================

# Step 1: Build the JAR with Maven & Eclipse Temurin Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR from build stage
COPY --from=build /app/target/smart-energy-monitoring-system-1.0.0.jar app.jar

# Default fallback port (Render will inject its own $PORT at runtime)
ENV PORT=8081
EXPOSE 8081

# Launch Spring Boot with system properties
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

