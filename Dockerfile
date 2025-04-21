FROM openjdk:11-jre-slim

WORKDIR /app

# Add the Maven-built application JAR 
COPY target/ticket-customer-service-1.0.0.jar app.jar

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=ticket_service_db \
    DB_USERNAME=root \
    DB_PASSWORD=password \
    EVENT_SERVICE_URL=http://event-service:8081/event-service \
    EVENT_SERVICE_API_KEY=your-api-key \
    OPENROUTER_API_KEY=your-openrouter-api-key \
    ALLOWED_ORIGINS=*

# Expose the application port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]