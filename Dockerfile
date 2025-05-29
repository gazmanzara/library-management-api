# Use an official Maven image to build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy the pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the Spring Boot app
RUN mvn clean package -DskipTests

# Use a minimal JDK runtime to run the application
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/library-0.0.1-SNAPSHOT.jar app.jar

# Expose port (match your Spring Boot server.port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
