package com.eventticket.ticket.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ChatbotResponse {
    private String answer;
    private List<FAQ> relatedFaq;
    private Double confidence;

    // Getters and Setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<FAQ> getRelatedFaq() {
        return relatedFaq;
    }

    public void setRelatedFaq(List<FAQ> relatedFaq) {
        this.relatedFaq = relatedFaq;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    @Data
    public static class FAQ {
        private String question;
        private String id;

        // Getters and Setters
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}