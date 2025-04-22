package com.eventticket.ticket.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ChatbotRequest {

    @NotBlank(message = "Query is required")
    private String query;

    private Long userId;

    private String sessionId;

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