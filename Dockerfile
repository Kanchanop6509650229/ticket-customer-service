# Use a specific version for better security and reproducibility
FROM amazoncorretto:11.0.21-alpine3.18

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Add the Maven-built application JAR
COPY target/ticket-customer-service-1.0.0.jar app.jar

# Set ownership to the non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Environment variables with defaults for non-sensitive settings only
# Sensitive settings should be provided at runtime via environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=ticket_service_db \
    ALLOWED_ORIGINS=*

# Expose the application port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/ticket-service/actuator/health || exit 1

# Run the application with security options
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]