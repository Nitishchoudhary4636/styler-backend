@echo off
echo Starting Styler Backend in DEVELOPMENT mode with H2 database...
echo H2 Console available at: http://localhost:8080/h2-console
echo JDBC URL: jdbc:h2:mem:stylerdb
echo Username: sa
echo Password: password
echo.
java -Dspring.profiles.active=dev -Dserver.port=8080 -jar target\styler-backend-1.0.0.jar