package com.eventticket.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {
    private String id;
    private String name;
    private String description;
    private String venue;
    private LocalDate date;
    private String time;
    private Integer duration;
    private String organizer;
    private String category;
    private List<String> artists;
    private String status;
    private String imageUrl;
    private List<TicketTypeInfo> ticketTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TicketTypeInfo {
        private String type;
        private double price;
        private List<String> benefits;
    }
}