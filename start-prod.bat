@echo off
echo Starting Styler Backend in PRODUCTION mode with PostgreSQL...
echo Make sure PostgreSQL is running and configured
echo.
java -Dspring.profiles.active=prod -Dserver.port=8080 -jar target\styler-backend-1.0.0.jar