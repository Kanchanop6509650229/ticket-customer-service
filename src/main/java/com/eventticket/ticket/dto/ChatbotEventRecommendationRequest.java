package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotEventRecommendationRequest {

    @NotBlank(message = "Query is required")
    @Schema(description = "The user's question or query about event recommendations", example = "Is there any rock concerts are scheduled in Bangkok during July 2025? I'm particularly interested in the venue, start time, and duration of these events.")
    private String query;

    @Schema(description = "ID of the user making the query", example = "3")
    private Long userId;

    @Schema(description = "Session ID for tracking conversation context", example = "session123")
    private String sessionId;

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
}
