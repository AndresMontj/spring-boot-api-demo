# Use official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the packaged JAR file into the container (use correct name)
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS=""

# Run the JAR file (use exec form for better signal handling)
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar