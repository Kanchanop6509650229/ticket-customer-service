package com.eventticket.ticket.config;

import com.eventticket.ticket.security.MockAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration that completely disables authentication requirements.
 * This allows all endpoints to be accessed without authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public MockAuthenticationFilter mockAuthenticationFilter() {
        return new MockAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection
        http.csrf(csrf -> csrf.disable());

        // Allow all requests without authentication
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        // Add our mock authentication filter
        http.addFilterBefore(mockAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}