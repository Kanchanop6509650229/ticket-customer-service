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

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(int soldTickets) {
        this.soldTickets = soldTickets;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public Map<String, Integer> getAvailabilityByType() {
        return availabilityByType;
    }

    public void setAvailabilityByType(Map<String, Integer> availabilityByType) {
        this.availabilityByType = availabilityByType;
    }
}