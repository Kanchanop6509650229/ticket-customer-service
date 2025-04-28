package com.eventticket.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatbotResponse {
    @Schema(description = "The chatbot's response to the user's query", example = "The BNK48 Concert starts at 18:00 on June 15, 2025. Doors open 1 hour before the show.")
    private String answer;

    @Schema(description = "List of related frequently asked questions", example = "[{\"question\": \"What time do doors open?\", \"id\": \"faq-123\"}]")
    private List<FAQ> relatedFaq;

    @Schema(description = "Confidence score of the answer (0.0 to 1.0)", example = "0.98")
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
        @Schema(description = "The frequently asked question", example = "What time do doors open?")
        private String question;

        @Schema(description = "Unique identifier for the FAQ", example = "faq-123")
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