# Use lightweight Alpine base image with OpenJDK 17
FROM openjdk:17-jdk-alpine

# Create a non-root user (optional but recommended for security)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Create /app directory
WORKDIR /app

# Copy JAR from target folder (argument from Maven build)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
