# Styler E-commerce Backend

A simple Java Spring Boot backend for the Styler e-commerce application, designed for collecting customer data and behavior analytics - perfect for Salesforce Marketing Cloud integration.

## 🚀 Features

- **User Management**: Registration, authentication, and profile management
- **Order Processing**: Complete order lifecycle management
- **CORS Enabled**: Ready for frontend integration
- **H2 Database**: In-memory database for development

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## 🛠 Quick Start

### 1. Navigate to Backend Directory
```bash
cd styler-backend
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

### 3. Test the API
```bash
# Health check
curl http://localhost:8080/api/users

# Register a user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

## 📊 Database Console

Access H2 database console at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:stylerdb`
- **Username**: `sa`
- **Password**: `password`

## 🔌 API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User authentication
- `GET /api/users/{email}` - Get user profile
- `PUT /api/users/{email}` - Update user profile

### Order Management
- `POST /api/orders` - Create new order
- `GET /api/orders/{orderId}` - Get order details
- `GET /api/orders/user/{email}` - Get user orders
- `PUT /api/orders/{orderId}/status` - Update order status

## 📱 Frontend Integration

### Update Your Frontend JavaScript

Replace localStorage calls with API calls:

```javascript
// Replace this localStorage code in your frontend:
// localStorage.setItem('currentUser', email);

// With API calls:
async function loginUser(email, password) {
  const response = await fetch('http://localhost:8080/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  const result = await response.json();
  if (result.success) {
    localStorage.setItem('currentUser', email);
  }
  return result;
}

// Create order
async function createOrder(orderData) {
  const response = await fetch('http://localhost:8080/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(orderData)
  });
  
  return await response.json();
}
```

## 🔧 Configuration

### Database (Production)
To use MySQL/PostgreSQL instead of H2, update `application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/styler_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### Environment Variables
```bash
export DB_URL=jdbc:mysql://localhost:3306/styler_db
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

## 📈 Monitoring & Analytics

The backend provides basic analytics for:
- User registration trends
- Order statistics and revenue tracking

## 🔒 Security Notes

- Currently uses basic authentication (suitable for development)
- For production, implement JWT tokens or OAuth2
- Add password hashing with BCrypt
- Implement rate limiting
- Add input validation and sanitization

## 🚀 Deployment

### JAR Deployment
```bash
mvn clean package
java -jar target/styler-backend-1.0.0.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/styler-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🤝 Contributing

This backend is designed to complement your existing Styler frontend and provide the foundation for Marketing Cloud integration. Feel free to extend it with additional features as needed.

## 📝 License

This project is for educational and development purposes.