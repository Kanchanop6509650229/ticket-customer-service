package com.eventticket.ticket.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ChatbotResponse {
    private String answer;
    private List<FAQ> relatedFaq;
    private Double confidence;
    
    @Data
    public static class FAQ {
        private String question;
        private String id;
    }
}