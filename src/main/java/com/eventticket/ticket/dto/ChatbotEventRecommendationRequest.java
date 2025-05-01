package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotEventRecommendationRequest {

    @NotBlank(message = "Query is required")
    @Schema(description = "The user's question or query about event recommendations", example = "Recommend me some concerts")
    private String query;

    @Schema(description = "ID of the user making the query", example = "3")
    private Long userId;

    @Schema(description = "Session ID for tracking conversation context", example = "session123")
    private String sessionId;

    @Schema(description = "Category of events to recommend", example = "Concert")
    private String category;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
