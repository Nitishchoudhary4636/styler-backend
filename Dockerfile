# Use Maven image for building, then OpenJDK for runtime
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better caching)
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage with smaller image
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/styler-backend-1.0.0.jar ./app.jar

# Expose port
EXPOSE $PORT

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=render-prod

# Run the application
CMD java -Dserver.port=$PORT -Dspring.profiles.active=render-prod -Djava.awt.headless=true -Xmx512m -jar app.jar