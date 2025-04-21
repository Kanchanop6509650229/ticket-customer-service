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

   Update `application-dev.properties` with your database credentials.

3. **Set API Keys**

   Update the following configuration in `application-dev.properties`:
   - `openrouter.api.key`: Your OpenRouter API key

4. **Build the application**

   ```bash
   mvn clean package
   ```

5. **Run the application**

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

Run the container:

```bash
docker run -p 8082:8082 \
  -e DB_HOST=mysql-host \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  -e OPENROUTER_API_KEY=your-api-key \
  -e EVENT_SERVICE_URL=http://event-service-host:8081/event-service \
  -e EVENT_SERVICE_API_KEY=your-event-service-api-key \
  ticket-customer-service:latest
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

The API is secured using JWT authentication. To access protected endpoints, you'll need to:

1. Authenticate with the `/api/auth/login` endpoint (provided by a separate auth service)
2. Include the JWT token in the Authorization header for subsequent requests:
   ```
   Authorization: Bearer <token>
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