# Ticket Customer Service

This service is part of the Event/Concert Ticket Management System, responsible for managing tickets, bookings, and customer support through a chatbot.

## Services

The Ticket Customer Service provides the following key services:

1. **Ticket Information Service** - Provides information about tickets, including availability
2. **Ticket Booking Service** - Manages the booking and purchase of tickets
3. **Ticket Support Chatbot** - Provides customer support using LLM technology

## Technology Stack

- Java 11
- Spring Boot 2.7.3
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Spring HATEOAS
- OpenAPI/Swagger for documentation
- Flyway for database migrations
- OpenRouter API for LLM chatbot (in place of OpenAI)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11
- Maven 3.6+
- MySQL 8.0+
- Docker (optional)

### Setup and Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/eventticket/ticket-customer-service.git
   cd ticket-customer-service
   ```

2. **Configure database**

   Create a MySQL database:

   ```sql
   CREATE DATABASE ticket_service_db;
   ```

3. **Set up configuration files securely**

   Copy the template files to create your local configuration:

   ```bash
   cp src/main/resources/application-template.properties src/main/resources/application.properties
   cp src/main/resources/application-dev-template.properties src/main/resources/application-dev.properties
   cp src/main/resources/application-prod-template.properties src/main/resources/application-prod.properties
   ```

   Update these files with your actual credentials and secrets. These files are excluded from git by the .gitignore file.

4. **Set API Keys and Secrets**

   Update the following configuration in your local `application-dev.properties`:
   - `spring.datasource.username`: Your database username
   - `spring.datasource.password`: Your database password
   - `openrouter.api.key`: Your OpenRouter API key

   Update the following in your local `application.properties`:
   - `app.jwt.secret`: A strong, randomly generated JWT secret key
   - `event.service.api-key`: Your event service API key

5. **Build the application**

   ```bash
   mvn clean package
   ```

6. **Run the application**

   ```bash
   java -jar target/ticket-customer-service-1.0.0.jar
   ```

   Or use Spring Boot Maven plugin:

   ```bash
   mvn spring-boot:run
   ```

### Docker Deployment

Build the Docker image:

```bash
docker build -t ticket-customer-service:latest .
```

Run the container using environment variables (recommended for security):

```bash
# Create a .env file with your secrets (DO NOT commit this file)
cat > .env << EOL
DB_HOST=mysql-host
DB_PORT=3306
DB_NAME=ticket_service_db
DB_USERNAME=your-username
DB_PASSWORD=your-password
OPENROUTER_API_KEY=your-api-key
EVENT_SERVICE_URL=http://event-service-host:8081/event-service
EVENT_SERVICE_API_KEY=your-event-service-api-key
JWT_SECRET=your-secure-jwt-secret
ALLOWED_ORIGINS=https://your-frontend-domain.com
EOL

# Run the container with environment variables from file
docker run -p 8082:8082 --env-file .env ticket-customer-service:latest
```

Alternatively, you can use Docker Compose for a more complete setup (recommended):

```yaml
# docker-compose.yml
version: '3.8'

services:
  ticket-service:
    build: .
    ports:
      - "8082:8082"
    env_file:
      - .env
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql

volumes:
  mysql-data:
```

## API Documentation

Once the application is running, you can access the Swagger documentation at:

```
http://localhost:8082/ticket-service/swagger-ui.html
```

The API is divided into three main sections:

1. **Ticket API** - Endpoints for managing tickets
2. **Booking API** - Endpoints for booking management
3. **Chatbot API** - Endpoints for the ticket support chatbot

## Security

### Authentication

The API is secured using JWT authentication. To access protected endpoints, you'll need to:

1. Authenticate with the `/api/auth/login` endpoint (provided by a separate auth service)
2. Include the JWT token in the Authorization header for subsequent requests:
   ```
   Authorization: Bearer <token>
   ```

### Secure Configuration

**IMPORTANT: Never commit sensitive information to version control!**

This project uses the following security practices:

1. **Template Configuration Files**: Example configuration files with placeholders are provided as templates.
2. **Environment-Specific Properties**: Sensitive configuration is separated into environment-specific files.
3. **Environment Variables**: Production deployments use environment variables instead of configuration files.
4. **.gitignore**: Configuration files with secrets are excluded from version control.

### Secrets Management

For local development:
- Use the template files and create local copies that are not committed to git
- Store your secrets in application-dev.properties (excluded by .gitignore)

For production:
- Use environment variables or a secure secrets management service
- Never hardcode secrets in any files that might be committed
- Rotate secrets regularly

### Handling Secrets in CI/CD

When using CI/CD pipelines:
- Store secrets in your CI/CD platform's secure secrets storage
- Inject secrets as environment variables during build/deployment
- Never log or display secrets in build outputs

### Security Tools

This project includes several tools to help maintain security:

1. **Git Hooks**: Pre-commit hooks to prevent accidental commits of sensitive information
   ```bash
   # Install the git hooks
   git config core.hooksPath .git-hooks
   chmod +x .git-hooks/*
   ```

2. **Secret Generation**: Script to generate secure random values for configuration
   ```bash
   # Generate secure secrets
   ./scripts/generate-secrets.sh
   ```

3. **Secret Detection**: Script to check for potential secrets in the codebase
   ```bash
   # Check for potential secrets
   ./scripts/check-for-secrets.sh
   ```

4. **Template Files**: Example configuration files with placeholders instead of real secrets
   - `application-template.properties`
   - `application-dev-template.properties`
   - `application-prod-template.properties`
   - `.env.template`

5. **Secret Cleanup**: Guidance for cleaning up secrets that might have been committed
   ```bash
   # Get guidance on cleaning up secrets from git history
   ./scripts/cleanup-secrets.sh
   ```

## Testing

The application includes unit and integration tests. Run the tests with:

```bash
mvn test
```

## Communication with Event Service

This service communicates with the Event Service to:

1. Get event details
2. Check event status before booking
3. Search for events

For development, make sure the Event Service is running on its designated port (default: 8081).

## License

This project is licensed under the Apache License 2.0.