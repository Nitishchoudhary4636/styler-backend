# MySQL Setup Instructions

## Step 1: Update MySQL Password in application.properties
1. Open: styler-backend/src/main/resources/application.properties
2. Replace "your_mysql_password" with your actual MySQL root password
3. If you don't know your password, you can reset it using MySQL Workbench

## Step 2: Create Database (Choose one method)

### Method A: Using MySQL Workbench (Recommended for beginners)
1. Open MySQL Workbench
2. Connect to your local MySQL server
3. Run this SQL command:
   ```sql
   CREATE DATABASE IF NOT EXISTS styler_db;
   USE styler_db;
   ```

### Method B: Using Command Line
1. Open Command Prompt (not PowerShell)
2. Navigate to: C:\Program Files\MySQL\MySQL Server 8.0\bin
3. Run: mysql -u root -p
4. Enter your password
5. Run: CREATE DATABASE IF NOT EXISTS styler_db;

## Step 3: Verify Database
After creating the database, you should see "styler_db" in your databases list.
The Spring Boot application will automatically create the tables when it starts.