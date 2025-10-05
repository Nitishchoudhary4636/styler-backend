# Database Initialization Guide for Railway

## Steps to Initialize PostgreSQL Tables

After your Railway deployment is successful and health checks are passing:

### 1. Test the Application Health
```bash
curl https://your-railway-app.railway.app/health
```
Should return: `{"status":"UP","mode":"railway-fast"}`

### 2. Initialize Database Tables
```bash
curl -X POST https://your-railway-app.railway.app/admin/init-database \
  -H "Content-Type: application/json"
```

Expected Response:
```json
{
  "success": true,
  "message": "Database tables initialized successfully",
  "tables": ["users", "orders", "order_items", "shipping_addresses"],
  "mode": "railway-fast"
}
```

### 3. Verify Tables Were Created
You can check in Railway's PostgreSQL dashboard or connect to the database to verify the tables exist:

- `users` - User accounts with authentication
- `orders` - Order records 
- `order_items` - Individual items within orders
- `shipping_addresses` - Delivery addresses for orders

### 4. Test API Endpoints (Optional)
After table creation, you can test the in-memory API:

**Register User:**
```bash
curl -X POST https://your-railway-app.railway.app/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","fullName":"Test User"}'
```

**Login:**
```bash
curl -X POST https://your-railway-app.railway.app/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## Why This Approach?

1. **Fast Startup**: Railway health checks pass quickly with railway-fast profile
2. **Manual Control**: Database tables are created only when needed
3. **Zero Downtime**: Table creation happens after successful deployment
4. **Future-Ready**: Tables are ready for database-enabled profile migration

## Next Steps

Once tables are created, you can:
1. Keep using in-memory storage for testing
2. Switch to a database-enabled profile for persistent storage
3. Migrate data from in-memory to PostgreSQL if needed