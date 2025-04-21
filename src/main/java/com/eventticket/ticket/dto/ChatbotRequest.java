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
}