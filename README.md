# Spring Boot 3 Image CRUD API

A comprehensive RESTful API for image management with user authentication, built with Spring Boot 3, PostgreSQL, and JWT.

## Features

- **Complete CRUD Operations**: Create, Read, Update, and Delete images
- **Secure Authentication**: JWT token-based authentication system
- **User-Based Access Control**: Users can only modify their own images
- **Image Storage**: Store images with metadata (name, description, size, etc.)
- **Search Capability**: Search images by name
- **Comprehensive Testing**: Complete unit and integration tests
- **Exception Handling**: Global exception handling with proper HTTP responses
- **Data Validation**: Input validation using Spring Validation
- **Database Integration**: PostgreSQL with Spring Data JPA

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT Authentication**
- **Maven**
- **JUnit 5 & TestContainers**
- **Lombok**

## Prerequisites

Before you begin, ensure you have the following installed:
- JDK 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git (optional)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/spring-boot-image-crud.git
cd spring-boot-image-crud
```

### 2. Run with Docker (Recommended)

The easiest way to get started is using Docker:

#### Using Docker Compose

1. Create a `docker-compose.yml` file in the project root:

```yaml
version: '3.8'

services:
  postgres:
    build:
      context: ./postgres
      dockerfile: Dockerfile
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/imagecrud
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - app-network

volumes:
  postgres-data:

networks:
  app-network:
    driver: bridge
```

2. Create a `postgres` directory and add a Dockerfile and init script:

```
mkdir -p postgres
```

Create `postgres/Dockerfile`:

```dockerfile
# Use the official PostgreSQL image from Docker Hub
FROM postgres:14-alpine

# Set environment variables for PostgreSQL
ENV POSTGRES_DB=imagecrud
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# Create directories for persisting data and for initialization scripts
VOLUME /var/lib/postgresql/data
RUN mkdir -p /docker-entrypoint-initdb.d

# Copy initialization scripts
COPY init-db.sql /docker-entrypoint-initdb.d/

# Expose PostgreSQL port
EXPOSE 5432
```

Create `postgres/init-db.sql`:

```sql
-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create images table if not exists
CREATE TABLE IF NOT EXISTS images (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_images_user_id ON images(user_id);

-- Grant permissions to the postgres user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
```

3. Create a Dockerfile for the Spring Boot application in the project root:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

# Copy maven files first for better caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create directory for image uploads
RUN mkdir -p /app/uploads/images

# Copy JAR file
COPY --from=build /app/target/*.jar app.jar

# Set entrypoint
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

4. Start the application with Docker Compose:

```bash
docker-compose up
```

The application will be available at `http://localhost:8080`.

### 3. Running PostgreSQL with Docker Only

If you want to run just the PostgreSQL database in Docker and the application locally:

```bash
# Build the PostgreSQL Docker image
cd postgres
docker build -t imagecrud-postgres .

# Run the PostgreSQL container
docker run -d -p 5432:5432 --name imagecrud-db imagecrud-postgres
```

Then update your application.yml:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/imagecrud
    username: postgres
    password: postgres
```

### 4. Manual Setup (without Docker)

#### Configure PostgreSQL

Create a PostgreSQL database:

```sql
CREATE DATABASE imagecrud;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE imagecrud TO postgres;
```

Alternatively, update the `application.yml` with your own database configuration.

#### Build and Run

```bash
mvn clean package
java -jar target/image-crud-0.0.1-SNAPSHOT.jar
```

Or simply use:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Project Structure

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.imagecrud
│   │   │       ├── config              # Configuration classes
│   │   │       ├── controller          # REST controllers
│   │   │       ├── dto                 # Data Transfer Objects
│   │   │       ├── exception           # Custom exceptions and handler
│   │   │       ├── model               # Entity models
│   │   │       ├── repository          # JPA repositories
│   │   │       ├── service             # Business logic
│   │   │       └── util                # Utility classes
│   │   └── resources
│   │       ├── application.yml         # Application configuration
│   │       └── schema.sql              # Database schema
│   └── test
│       ├── java                        # Unit & integration tests
│       └── resources
│           └── application-test.yml    # Test configuration
```

## API Endpoints

### Authentication

| Method | URL                     | Description         | Request Body         | Response                         |
|--------|-------------------------|---------------------|----------------------|----------------------------------|
| POST   | `/api/auth/register`    | Register new user   | `AuthRequest`        | `AuthResponse` (with JWT token)  |
| POST   | `/api/auth/login`       | Authenticate user   | `AuthRequest`        | `AuthResponse` (with JWT token)  |
| GET    | `/api/auth/test`        | Test auth service   | -                    | `MessageResponse`                |

### Images

| Method | URL                     | Description         | Request Body/Param   | Response                         |
|--------|-------------------------|---------------------|----------------------|----------------------------------|
| GET    | `/api/images`           | Get all images      | -                    | List of `ImageDto`               |
| GET    | `/api/images/{id}`      | Get image by ID     | `id` (path)          | `ImageDto`                       |
| GET    | `/api/images/user`      | Get user images     | -                    | List of `ImageDto`               |
| GET    | `/api/images/search`    | Search by name      | `name` (query)       | List of `ImageDto`               |
| POST   | `/api/images`           | Upload new image    | Multipart form       | `ImageDto`                       |
| PUT    | `/api/images/{id}`      | Update image        | `id` (path), `ImageDto` | `ImageDto`                   |
| DELETE | `/api/images/{id}`      | Delete image        | `id` (path)          | `MessageResponse`                |

## Authentication Flow

1. **Register a User**: Send a POST request to `/api/auth/register` with username and password
2. **Login**: Send a POST request to `/api/auth/login` to get a JWT token
3. **Use the Token**: Include the token in the Authorization header: `Bearer {token}`

## Example Requests

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Upload an Image

```bash
curl -X POST http://localhost:8080/api/images \
  -H "Authorization: Bearer {your_jwt_token}" \
  -F "file=@/path/to/image.jpg" \
  -F "name=My Image" \
  -F "description=Beautiful landscape photo"
```

### Get User's Images

```bash
curl -X GET http://localhost:8080/api/images/user \
  -H "Authorization: Bearer {your_jwt_token}"
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Unit Tests Only

```bash
mvn test -Dtest=*Test
```

### Run Integration Tests Only

```bash
mvn test -Dtest=*IntegrationTest
```

## Security Considerations

- JWT tokens are signed with HMAC SHA-256
- Passwords are hashed using BCrypt
- Authorization checks ensure users can only access their own resources
- Input validation prevents invalid data
- Exception handling prevents information leakage
- File uploads are validated for type and size

## Best Practices Implemented

- **Layered Architecture**: Clear separation of controllers, services, repositories
- **DTO Pattern**: Separates API contracts from internal entities
- **Global Exception Handling**: Centralized error responses
- **Comprehensive Logging**: Throughout the application
- **Unit & Integration Testing**: High test coverage
- **Transaction Management**: For data consistency
- **Secure Password Storage**: Using password hashing
- **JWT Best Practices**: Secure token handling
- **Code Documentation**: Clear documentation throughout

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contribution

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request