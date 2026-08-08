# Spring Boot API Demo

A comprehensive Spring Boot project demonstrating best practices for building REST APIs with:

- **RESTful API Design** - Clean, well-documented endpoints
- **Circuit Breaker Pattern** - Using Resilience4j for fault tolerance
- **JWT Authentication** - Stateless authentication with JSON Web Tokens
- **Spring Security** - Comprehensive security configuration
- **Best Practices** - Exception handling, validation, logging, testing

## Features

### Core Functionality

- CRUD operations for User management
- RESTful API design with proper HTTP status codes
- Request/response validation using Jakarta Validation
- Global exception handling for consistent error responses

### Security

- JWT-based authentication for stateless API security
- Role-based access control (RBAC) with Admin/User roles
- Password encoding using BCrypt
- Stateless authentication suitable for microservices

### Resilience Patterns

- Circuit Breaker using Resilience4j to prevent cascade failures
- Retry mechanism for transient failures
- Rate limiting to protect against abuse
- Timeout configuration for external service calls

### API Documentation

- OpenAPI 3.0/Swagger UI integration
- Interactive API documentation at `/swagger-ui.html`
- Detailed API annotations for better developer experience

### Observability

- Structured logging with AOP for method-level tracing
- Actuator endpoints for monitoring and management
- Circuit breaker and rate limiter metrics exposed via Actuator

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. The application will start on port 8080
6. Access the API documentation at: http://localhost:8080/swagger-ui.html

### Configuration

The application can be configured using `application.yml` or environment variables:

#### Database (H2 in-memory)

- Console available at: http://localhost:8080/api/h2-console
- JDBC URL: jdbc:h2:mem:testdb

#### JWT Settings

- `JWT_SECRET`: Base64-encoded secret key for JWT signing
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 24 hours)

## API Endpoints

### Authentication

- `POST /api/auth/login` - Authenticate user and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### User Management (Requires JWT Authentication)

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update existing user
- `DELETE /api/users/{id}` - Delete user

### Circuit Breaker Demo (Requires JWT Authentication)

- `GET /api/circuit-breaker/call` - Call external service with circuit breaker
- `GET /api/circuit-breaker/retry` - Call with retry pattern
- `GET /api/circuit-breaker/rate-limited` - Call with rate limiting

## Building for Production

To create a production-ready JAR:

```bash
mvn clean package -DskipTests
```

The executable JAR will be available in the `target/` directory.

## Testing

All tests pass, including controller, service, and repository tests.

Run the test suite:

```bash
mvn test
```

## Project Structure

```
src/main/java/com/example/demo/
├── controller        # REST controllers
├── dto               # Data Transfer Objects
├── model             # JPA entities
├── repository        # Spring Data repositories
├── service           # Business logic
├── config            # Configuration classes
├── security          # Security configuration
├── exception         # Global exception handling
├── aspect            # AOP aspects (logging)
���└── util              # Utility classes
```

## Technologies Used

- **Spring Boot 4.1.0** - Framework foundation
- **Spring Data JPA** - ORM and data access
- **Spring Security** - Authentication and authorization
- **Jakarta Validation** - Input validation
- **Resilience4j** - Circuit breaker, retry, rate limiting
- **JJWT** - JSON Web Token implementation
- **Springdoc OpenAPI** - API documentation (Swagger UI)
- **H2 Database** - In-memory database for development
- **Lombok** - Reduced boilerplate code
- **SLF4J & Logback** - Logging framework
- **JUnit 5** - Testing framework

## Best Practices Demonstrated

1. **API Design**
    - Consistent RESTful endpoints
    - Proper HTTP status codes
    - Meaningful error responses
    - API versioning ready

2. **Security**
    - Stateless JWT authentication
    - Password encoding
    - Role-based access control
    - Stateless security suitable for microservices

3. **Resilience**
    - Circuit breaker pattern
    - Retry mechanism
    - Rate limiting
    - Timeout handling

4. **Code Quality**
    - Clean architecture separation
    - Dependency injection
    - DTO pattern for API contracts
    - Utility classes for common functions
    - Global exception handling
    - AOP for cross-cutting concerns (logging)
    - Comprehensive validation
    - Proper logging practices

5. **Observability**
    - Structured logging
    - Actuator endpoints
    - Metrics exposure
    - Distributed tracing ready

6. **Testing**
    - Unit tests for controllers
    - Test-friendly design
    - Mockito-ready services

## License

This project is for educational purposes demonstrating Spring Boot best practices.

---
*Built with Spring Boot 4.1.0 and Java 25*

## Usage Examples

### Authentication

```bash
# Login with admin credentials
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
# Returns: {"role":"USER","token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc4NjIxMzE2NCwiZXhwIjoxNzg2Mjk5NTY0fQ.YkfplaCqF_pLoSX4obxArAiaXr3LAaTEb1r0T2NnbaE","type":"Bearer","username":"admin"}

# Login with user credentials
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"user123"}'
# Returns: {"role":"USER","token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzg2MjEwNDY3LCJleHAiOjE3ODYyOTY4Njd9.VZBX9HbUdk2Tjh_3od77J0wlZ7U-U_8xrypVnWIMZi8","type":"Bearer","username":"user"}
```

### Accessing Protected Endpoints

```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | jq -r .token)

# Use token to access actuator endpoint
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/actuator/health
# Returns: {"components":{"db":{"details":{"database":"H2","validationQuery":"isValid()"},"status":"UP"},...,"status":"UP"}

# Access users endpoint (note the double /api prefix due to controller mapping)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/api/users
# Returns: [{"id":1,"username":"admin","email":"admin@example.com","password":"$2a$10$...","firstName":"Admin","lastName":"User","role":"ADMIN"},...]
```

### API Documentation

```bash
# View Swagger UI (interactive documentation)
curl -s http://localhost:8080/api/swagger-ui/index.html
# Returns: 200 OK (HTML content for Swagger UI)

# View OpenAPI 3.0 specification
curl -s http://localhost:8080/api/v3/api-docs | head -20
# Returns: {"openapi":"3.0.1","info":{"title":"OpenAPI definition","version":"v0"},"servers":[{"url":"http://localhost:8080/api","description":"Generated server url"}],"tags":[{"name":"User Management","description":"APIs for managing users"},{"name":"Circuit Breaker","description":"APIs demonstrating circuit breaker pattern"}],"paths":{...}}
```