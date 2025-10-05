# Use official OpenJDK 17 runtime as the base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE $PORT

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=render-prod

# Run the application
CMD java -Dserver.port=$PORT -Dspring.profiles.active=render-prod -Djava.awt.headless=true -Xmx512m -jar target/styler-backend-1.0.0.jar