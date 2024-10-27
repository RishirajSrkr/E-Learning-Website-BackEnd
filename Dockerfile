# Use the official JDK 21 slim image from the Docker Hub
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the entire project
COPY . .

# Package the application
RUN mvn clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "target/bitbybit-0.0.1-SNAPSHOT.jar"]
