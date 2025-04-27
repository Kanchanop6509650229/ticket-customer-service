package com.eventticket.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration to disable method-level security.
 * This allows endpoints to be accessed without authentication even if they have @PreAuthorize annotations.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = false) // Disable @PreAuthorize annotations
public class MethodSecurityConfig {

    /**
     * Creates a mock user with all roles for testing purposes.
     * This user will be automatically used when no authentication is provided.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        User mockUser = new User(
            "mock-user",
            "{noop}password", // {noop} means no password encoding
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ORGANIZER")
            )
        );

        return new InMemoryUserDetailsManager(Collections.singletonList(mockUser));
    }
}
