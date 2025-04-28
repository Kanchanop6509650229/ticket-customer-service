package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class ChatbotFaqRequest {

    @NotBlank(message = "Query is required")
    @Schema(description = "The user's question or query for FAQs", example = "Are there discounts for group bookings?")
    private String query;

    @Schema(description = "ID of the user making the query", example = "3")
    private Long userId;

    @Schema(description = "Session ID for tracking conversation context", example = "session123")
    private String sessionId;

    @Schema(description = "ID of the event the user is asking about", example = "1")
    private String eventId;

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
