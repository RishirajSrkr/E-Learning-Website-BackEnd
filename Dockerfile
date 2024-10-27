# Use the official Maven image with JDK 21
FROM maven:3.8.6-openjdk-21-slim AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the entire project
COPY . .

# Package the application
RUN mvn clean package -DskipTests

# Use the official JDK image for the final stage
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar from the builder stage
COPY --from=builder /app/target/bitbybit-0.0.1-SNAPSHOT.jar bitbybit-0.0.1-SNAPSHOT.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "bitbybit-0.0.1-SNAPSHOT.jar"]
