@echo off
echo Starting Styler Backend in RAILWAY mode (no database)...
echo This mode is for Railway deployment health checks only
echo For full functionality, use production mode with database
echo.
echo Railway mode endpoints:
echo - Health: http://localhost:8080/health
echo - API Health: http://localhost:8080/api/health
echo - Root: http://localhost:8080/
echo.

java -Dspring.profiles.active=railway -jar target/styler-backend-1.0.0.jar

pause