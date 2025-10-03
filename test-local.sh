#!/bin/bash
# Simple local test script for Styler Backend

echo "Testing Styler Backend locally..."

# Start the application in background
echo "Starting application..."
java -Dspring.profiles.active=dev -Dserver.port=8080 -jar target/styler-backend-1.0.0.jar &
APP_PID=$!

# Wait for startup
echo "Waiting for application to start..."
sleep 15

# Test health endpoints
echo "Testing health endpoints..."

echo "1. Testing /health endpoint:"
curl -s http://localhost:8080/health || echo "FAILED: /health"

echo -e "\n2. Testing /api/health endpoint:"
curl -s http://localhost:8080/api/health || echo "FAILED: /api/health"

echo -e "\n3. Testing root endpoint:"
curl -s http://localhost:8080/ || echo "FAILED: /"

# Clean up
echo -e "\nStopping application..."
kill $APP_PID

echo "Test completed!"