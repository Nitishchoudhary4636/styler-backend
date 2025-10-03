#!/bin/bash

echo "Starting Styler Backend for Railway deployment..."
echo "Java Version:"
java -version

echo "Environment Variables:"
echo "PORT: $PORT"
echo "DATABASE_URL: ${DATABASE_URL:-(not set)}"
echo "PGUSER: ${PGUSER:-(not set)}"

echo "Starting application..."
exec java -Dserver.port=$PORT \
          -Dspring.profiles.active=prod \
          -Djava.awt.headless=true \
          -Xmx512m \
          -Dlogging.level.com.styler=INFO \
          -Dlogging.level.org.springframework.boot=INFO \
          -jar target/styler-backend-1.0.0.jar