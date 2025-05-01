# Ticket Customer Service

A Spring Boot application for managing tickets and customer bookings for events and concerts.

## Overview

The Ticket Customer Service is part of a microservices architecture for an event ticketing system. This service handles:

- Ticket management
- Booking processing
- Customer support via AI-powered chatbot
- User authentication and authorization

## Technologies

- **Java 21**
- **Spring Boot 3.4.4**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with MySQL
- **Flyway** for database migrations
- **DeepSeek AI** for chatbot functionality
- **Swagger/OpenAPI** for API documentation
- **Docker** for containerization

## Prerequisites

- Java 21
- Maven
- MySQL 8.0+
- Docker (optional, for containerized deployment)

## Configuration

The application uses a properties file for configuration. Copy the template file and adjust as needed:

```bash
cp src/main/resources/application-template.properties src/main/resources/application.properties
```

### Key Configuration Properties

- **Server**: Port and context path
- **Database**: Connection details
- **JWT**: Secret key and token configuration
- **Event Service**: URL and API key for the event service
- **DeepSeek API**: API key and model configuration

## Database Setup

The application uses Flyway for database migrations. The migrations will:

1. Create necessary tables (users, roles, tickets, bookings, payments, chat_history)
2. Insert initial data (roles, sample users)

Ensure your MySQL server is running and the database exists:

```sql
CREATE DATABASE ticket_service_db;
```

### Mock Data

The application includes mock data for testing and development purposes:

#### Users
- **Admin User**: Username: `admin`, Password: `admin123`, Role: `ROLE_ADMIN`
- **Organizer User**: Username: `organizer`, Password: `organizer123`, Role: `ROLE_ORGANIZER`
- **Regular User**: Username: `user`, Password: `user123`, Role: `ROLE_USER`

#### Events
The mock data includes tickets for several sample events:
- **BNK48 Concert 2025** (event ID: `1`)
  - 5 VIP tickets (Section A, Seats A1-A5) at ฿5,000 each
  - 5 Regular tickets (Section B, Seats B1-B5) at ฿2,500 each
  - 5 Economy tickets (Section C, Seats C1-C5) at ฿1,500 each
- **BNK48 Handshake Event** (event ID: `2`)
  - 5 Standard tickets (Main section, Seats M1-M5) at ฿1,500 each
- **Slot Machine Live in Bangkok** (event ID: `3`)
  - 3 VIP tickets (Section A, Seats A1-A3) at ฿3,500 each
  - 3 Regular tickets (Section B, Seats B1-B3) at ฿2,000 each
- **Thai Rock Festival 2025** (event ID: `4`)
  - 2 VIP tickets (Section A, Seats A1-A2) at ฿4,500 each
  - 2 Regular tickets (Section B, Seats B1-B2) at ฿2,500 each
  - 2 Standing tickets (Section C, Seats C1-C2) at ฿1,800 each

#### Bookings and Payments
- Sample booking for the regular user with 2 VIP tickets for the BNK48 Concert
- Payment record for the booking (฿10,000, status: COMPLETED)

#### Chat History
- Sample chat history entries with questions about refunds and event times

## Building and Running

### Using Maven

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/ticket-customer-service-1.0.0.jar
```

### Using Docker

```bash
# Build the Docker image
docker build -t ticket-customer-service .

# Run with Docker Compose
docker-compose up -d
```

## API Endpoints

The application provides the following main API endpoints:

- **Authentication**: `/api/auth/**`
- **Tickets**: `/api/tickets/**`
- **Bookings**: `/api/bookings/**`
- **Payments**: `/api/payments/**`
- **Chatbot**: `/api/chatbot/**`

### Detailed Endpoint List

| Endpoint | Method | Description | Authentication Required |
|----------|--------|-------------|------------------------|
| **Authentication** |
| `/api/auth/login` | POST | User login | No |
| `/api/auth/register` | POST | Register new user | No |
| **Tickets** |
| `/api/tickets/event/{eventId}` | GET | Get all tickets for an event | Yes (ADMIN, ORGANIZER) |
| `/api/tickets/public/availability/{eventId}` | GET | Get ticket availability for an event | No |
| `/api/tickets/{id}` | GET | Get ticket by ID | Yes (USER, ADMIN, ORGANIZER) |
| `/api/tickets/user/{userId}` | GET | Get tickets owned by a user | Yes (USER, ADMIN, owner) |
| `/api/tickets` | POST | Create a new ticket | Yes (ADMIN, ORGANIZER) |
| `/api/tickets/{id}/status` | PUT | Update ticket status | Yes (ADMIN, ORGANIZER) |
| `/api/tickets/{id}/assign/{userId}` | PUT | Assign ticket to a user | Yes (ADMIN, ORGANIZER) |
| **Bookings** |
| `/api/bookings` | GET | Get bookings for a user | Yes (USER, ADMIN) |
| `/api/bookings/{id}` | GET | Get booking by ID | Yes (USER, ADMIN, ORGANIZER) |
| `/api/bookings` | POST | Create a new booking | Yes (USER, ADMIN, ORGANIZER) |
| `/api/bookings/{id}/confirm` | PUT | Confirm a booking | Yes (USER, ADMIN, ORGANIZER) |
| `/api/bookings/{id}` | DELETE | Cancel a booking | Yes (USER, ADMIN, ORGANIZER) |
| `/api/bookings/{id}/payment` | POST | Process payment for a booking | Yes (USER, ADMIN) |
| **Chatbot** |
| `/api/chatbot/booking-help` | POST | Get help with ticket booking | Yes |
| `/api/chatbot/faq` | POST | Get answers to frequently asked questions | No |
| `/api/chatbot/event-info` | POST | Get information about events | Yes |
| `/api/chatbot/event-recommendations` | POST | Get event recommendations based on user query | Yes |

For detailed API documentation, access the Swagger UI at:
`http://localhost:8082/ticket-service/swagger-ui.html`

## Chatbot Functionality

The service includes an AI-powered chatbot for customer support using DeepSeek API. The chatbot can:

- Answer FAQs about events and ticketing
- Provide event-specific information
- Help with booking-related queries
- Recommend events based on natural language queries with intelligent parameter extraction

## Security

The application uses JWT for authentication. Public endpoints include:

- Authentication endpoints
- Public ticket information
- FAQ chatbot endpoint

All other endpoints require authentication.

## Environment Variables

When running with Docker, the following environment variables can be set:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `EVENT_SERVICE_URL`, `EVENT_SERVICE_API_KEY`
- `DEEPSEEK_API_KEY`
- `ALLOWED_ORIGINS`

## Development Notes

- The application uses Spring AI with DeepSeek API for chatbot functionality
- The service communicates with an Event Service for event details
- Database schema is managed through Flyway migrations
- Mock data is automatically loaded through Flyway migrations (V2__Insert_Initial_Data.sql)
- You can use the mock user credentials for testing the application without setting up additional data

## License

This project is licensed under the Apache License 2.0.
