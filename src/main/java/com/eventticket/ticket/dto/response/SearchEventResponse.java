package com.eventticket.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEventResponse {
    private List<EventSummary> results;
    private int totalResults;
    private Map<String, String> searchParams;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventSummary {
        private String id;
        private String name;
        private LocalDate date;
        private String venue;
        private TicketPriceRange ticketPrice;
        private String availability;
        private int ticketsLeft;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TicketPriceRange {
        private double min;
        private double max;
    }
}