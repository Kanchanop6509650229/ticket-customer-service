# Security Bypass Configuration

This document explains the security bypass configuration implemented in the ticket-customer-service application.

## Overview

The application has been configured to bypass user authentication for most endpoints, allowing you to use the API without implementing a user service. This is achieved through two main changes:

1. Modified `SecurityConfig.java` to make most API endpoints public
2. Added `MethodSecurityConfig.java` to bypass method-level security checks (`@PreAuthorize` annotations)

## Available Endpoints Without Authentication

The following endpoints can now be accessed without authentication:

### Ticket Endpoints
- `GET /api/tickets/event/{eventId}` - Get all tickets for an event
- `GET /api/tickets/public/availability/{eventId}` - Get ticket availability for an event
- `GET /api/tickets/{id}` - Get ticket by ID
- `GET /api/tickets/user/{userId}` - Get tickets owned by a user
- `POST /api/tickets` - Create a new ticket
- `PUT /api/tickets/{id}/status` - Update ticket status
- `PUT /api/tickets/{id}/assign/{userId}` - Assign ticket to a user

### Booking Endpoints
- `GET /api/bookings?userId={userId}` - Get bookings for a user
- `GET /api/bookings/{id}` - Get booking by ID
- `POST /api/bookings` - Create a new booking
- `PUT /api/bookings/{id}/confirm` - Confirm a booking
- `DELETE /api/bookings/{id}` - Cancel a booking
- `POST /api/bookings/{id}/payment` - Process payment for a booking

### Chatbot Endpoints
- `POST /api/chatbot/booking-help` - Get help with ticket booking
- `POST /api/chatbot/faq` - Get answers to frequently asked questions
- `POST /api/chatbot/event-info` - Get information about events

## How It Works

1. The `SecurityConfig` class has been modified to allow public access to most API endpoints
2. The `MethodSecurityConfig` class provides an `AuthorizationManager` bean that always returns `true`, effectively bypassing all `@PreAuthorize` annotations

## Security Considerations

This configuration is intended for development and testing purposes only. In a production environment, you should:

1. Implement proper user authentication
2. Remove the `MethodSecurityConfig` class
3. Restore the original security configuration in `SecurityConfig.java`

## Usage Example

You can now make direct API calls without authentication. For example:

```bash
# Get all tickets for an event
curl -X GET http://localhost:8082/ticket-service/api/tickets/event/event123

# Create a new booking
curl -X POST http://localhost:8082/ticket-service/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "eventId": "event123", "ticketIds": [1, 2], "totalAmount": 5000.00}'
```

## API Documentation

For detailed API documentation, access the Swagger UI at:
`http://localhost:8082/ticket-service/swagger-ui.html`
