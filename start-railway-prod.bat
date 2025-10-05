@echo off
echo Starting Styler Backend in RAILWAY-PRODUCTION mode...
echo This mode connects to PostgreSQL database but starts fast
echo.
echo Features:
echo - Full user registration with database
echo - Fast startup for Railway health checks
echo - PostgreSQL table creation
echo - All API endpoints available
echo.
echo Endpoints:
echo - Health: http://localhost:8080/health
echo - Registration: http://localhost:8080/api/users/register
echo - Login: http://localhost:8080/api/users/login
echo.

java -Dspring.profiles.active=railway-prod -jar target/styler-backend-1.0.0.jar

pause