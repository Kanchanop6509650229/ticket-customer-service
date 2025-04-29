package com.eventticket.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Response containing ticket sales statistics for an event")
public class TicketStatisticsResponse {

    @Schema(description = "ID of the event", example = "event123")
    private String eventId;

    @Schema(description = "Name of the event", example = "BNK48 Concert")
    private String eventName;

    @Schema(description = "Total number of tickets for the event", example = "1000")
    private int totalTickets;

    @Schema(description = "Number of tickets sold", example = "750")
    private int soldTickets;

    @Schema(description = "Number of tickets available", example = "250")
    private int availableTickets;

    @Schema(description = "Number of tickets reserved", example = "50")
    private int reservedTickets;

    @Schema(description = "Number of tickets checked in", example = "500")
    private int checkedInTickets;

    @Schema(description = "Number of tickets cancelled", example = "10")
    private int cancelledTickets;

    @Schema(description = "Percentage of tickets sold", example = "75.0")
    private double soldPercentage;

    @Schema(description = "Total revenue from ticket sales", example = "3750000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Breakdown of tickets by type", example = "{\"VIP\": 100, \"Regular\": 650}")
    private Map<String, Integer> ticketsByType;

    @Schema(description = "Revenue breakdown by ticket type", example = "{\"VIP\": 1000000.00, \"Regular\": 2750000.00}")
    private Map<String, BigDecimal> revenueByType;

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

    public int getReservedTickets() {
        return reservedTickets;
    }

    public void setReservedTickets(int reservedTickets) {
        this.reservedTickets = reservedTickets;
    }

    public int getCheckedInTickets() {
        return checkedInTickets;
    }

    public void setCheckedInTickets(int checkedInTickets) {
        this.checkedInTickets = checkedInTickets;
    }

    public int getCancelledTickets() {
        return cancelledTickets;
    }

    public void setCancelledTickets(int cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public double getSoldPercentage() {
        return soldPercentage;
    }

    public void setSoldPercentage(double soldPercentage) {
        this.soldPercentage = soldPercentage;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Integer> getTicketsByType() {
        return ticketsByType;
    }

    public void setTicketsByType(Map<String, Integer> ticketsByType) {
        this.ticketsByType = ticketsByType;
    }

    public Map<String, BigDecimal> getRevenueByType() {
        return revenueByType;
    }

    public void setRevenueByType(Map<String, BigDecimal> revenueByType) {
        this.revenueByType = revenueByType;
    }
}
