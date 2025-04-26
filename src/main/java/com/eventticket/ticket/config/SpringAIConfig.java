package com.eventticket.ticket.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Spring AI.
 *
 * This application uses OpenRouter instead of OpenAI directly.
 * We've set a dummy OpenAI API key in application.properties to satisfy
 * Spring AI's auto-configuration requirements, but we're not actually
 * using any OpenAI services directly.
 */
@Configuration
public class SpringAIConfig {
    // This class exists to document our approach to Spring AI configuration
    // The actual configuration is done in application.properties with:
    // spring.ai.openai.api-key=dummy-key-not-used
}
