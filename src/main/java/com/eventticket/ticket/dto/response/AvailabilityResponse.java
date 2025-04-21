package com.eventticket.ticket.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class AvailabilityResponse {
    private String eventId;
    private String eventName;
    private int totalTickets;
    private int soldTickets;
    private int availableTickets;
    private Map<String, Integer> availabilityByType;
}