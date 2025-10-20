# Stage 1: Build the application using a multi-stage build for efficiency
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml first to leverage Docker's layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# *** FIX APPLIED HERE ***
# Add execute permissions to the Maven Wrapper script.
# This is crucial for running it in a Linux-based container.
RUN chmod +x ./mvnw

# Download all dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of your application's source code
COPY src ./src

# Package the application, skipping the tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final, smaller runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Render injects the PORT environment variable. This EXPOSE is good practice.
EXPOSE 10000

# The command to run the application.
ENTRYPOINT ["java", "-jar", "app.jar"]

