package com.eventticket.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * Configuration to bypass method-level security checks.
 * This allows endpoints to be accessed without authentication even if they have @PreAuthorize annotations.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    /**
     * Creates an AuthorizationManager that always grants access.
     * This effectively bypasses all @PreAuthorize annotations in the controllers.
     */
    @Bean
    @Primary
    public AuthorizationManager<Object> authorizationManager() {
        return (Supplier<Authentication> authentication, Object object) -> new AuthorizationDecision(true);
    }
}
