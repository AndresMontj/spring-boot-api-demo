# Spring Boot API Demo

A comprehensive Spring Boot project demonstrating best practices for building REST APIs with:

- **RESTful API Design** - Clean, well-documented endpoints with versioning
- **API Versioning** - Explicit versioning in URL for user management endpoints (`/api/v1/users`)
- **Pagination & Sorting** - Spring Data Pageable support
- **Circuit Breaker Pattern** - Using Resilience4j for fault tolerance
- **JWT Authentication** - Stateless authentication with JSON Web Tokens
- **Spring Security** - Comprehensive security configuration
- **Data Auditing** - Automatic tracking of creation/modification details
- **Optimistic Locking** - Preventing lost updates with version field
- **Caching** - Spring Cache abstraction for improved performance
- **Connection Pooling** - HikariCP for efficient database connections
- **Best Practices** - Exception handling, validation, logging, testing

## Features

### Core Functionality

- CRUD operations for User management
- RESTful API design with proper HTTP status codes
- Request/response validation using Jakarta Validation
- Global exception handling for consistent error responses
- Pagination and sorting support for list endpoints
- API versioning (`/api/v1/users`)

### Security

- JWT-based authentication for stateless API security
- Role-based access control (RBAC) with Admin/User roles, enforced via Spring Security authorities
- Password encoding using BCrypt
- Stateless authentication suitable for microservices
- Password field marked `@JsonProperty(access = WRITE_ONLY)` in DTOs, so it can be accepted on input but is never included in API responses

### Data Integrity

- Audit fields (createdAt, updatedAt, createdBy, updatedBy) automatically populated
- Optimistic locking with `@Version` field to prevent lost updates
- JPA Auditing configuration with AuditorAware bean

### Resilience Patterns

- Circuit Breaker using Resilience4j to prevent cascade failures
- Retry mechanism for transient failures
- Rate limiting to protect against abuse
- Timeout configuration for external service calls

### Performance Optimizations

- Spring Cache abstraction enabled with cache names
- HikariCP connection pool configured for optimal database connections
- Explicit transaction boundaries for better control

### API Documentation

- OpenAPI 3.0/Swagger UI integration
- Interactive API documentation at `/swagger-ui.html`
- Detailed API annotations for better developer experience
- JWT security scheme documented in OpenAPI

### Observability

- Structured logging with AOP for method-level tracing
- Actuator endpoints for monitoring and management (`health`/`info` are public; all other endpoints require an authenticated ADMIN)
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
6. Access the API documentation at: http://localhost:8080/api/swagger-ui.html

### Configuration

The application can be configured using `application.yml` or environment variables:

#### JWT Settings

- `JWT_SECRET`: Base64-encoded secret key for JWT signing
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 24 hours)

#### Cache Settings

- Cache names: `users` (for user data)
- Cache type: Simple cache (can be changed to Redis, etc.)

#### Connection Pool (HikariCP)

- Maximum pool size: 20
- Minimum idle: 5
- Idle timeout: 30000ms
- Max lifetime: 1800000ms
- Connection timeout: 30000ms

## API Endpoints

### Authentication

- `POST /api/auth/login` - Authenticate user and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### User Management (Requires JWT Authentication)

- `GET /api/v1/users` - Get all users (with pagination)
- `GET /api/v1/users/{id}` - Get user by ID
- `POST /api/v1/users` - Create new user
- `PUT /api/v1/users/{id}` - Update existing user
- `DELETE /api/v1/users/{id}` - Delete user

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
└── aspect            # AOP aspects (logging)
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
- **Spring Cache** - Caching abstraction
- **HikariCP** - JDBC connection pool

## Best Practices Demonstrated

1. **API Design**
   - Consistent RESTful endpoints with versioning
   - Proper HTTP status codes
   - Meaningful error responses
   - Pagination and sorting support

2. **Security**
   - Stateless JWT authentication
   - Password encoding
   - Role-based access control, enforced via Spring Security authorities
   - Password field hidden in API responses (`@JsonProperty(access = WRITE_ONLY)`)
   - Stateless security suitable for microservices

3. **Data Integrity**
   - Automatic audit field population
   - Optimistic locking to prevent lost updates
   - JPA Auditing configuration

4. **Resilience**
   - Circuit breaker pattern
   - Retry mechanism
   - Rate limiting
   - Timeout handling

5. **Performance**
   - Spring Cache abstraction
   - HikariCP connection pooling
   - Explicit transaction boundaries

6. **Code Quality**
   - Clean architecture separation
   - Dependency injection (constructor-based)
   - DTO pattern for API contracts
   - Global exception handling
   - AOP for cross-cutting concerns (logging)
   - Comprehensive validation
   - Proper logging practices

7. **Observability**
   - Structured logging
   - Actuator endpoints (admin-restricted beyond health/info)
   - Metrics exposure

8. **Testing**
   - Unit tests for controllers, services, repositories
   - Test-friendly design
   - Mockito-ready services
   - Proper JSON path assertions for paginated responses

## License

This project is for educational purposes demonstrating Spring Boot best practices.

---

*Built with Spring Boot 4.1.0 and Java 25*