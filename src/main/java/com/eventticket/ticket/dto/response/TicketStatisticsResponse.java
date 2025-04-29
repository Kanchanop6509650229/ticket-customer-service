package com.eventticket.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketStatisticsResponse {
    private String eventId;
    private String eventName;
    
    // Ticket counts
    private int totalTickets;
    private int availableTickets;
    private int reservedTickets;
    private int soldTickets;
    private int checkedInTickets;
    private int cancelledTickets;
    
    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal potentialRevenue;
    
    // Breakdown by ticket type
    private Map<String, TypeStatistics> statisticsByType;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStatistics {
        private String type;
        private int total;
        private int available;
        private int reserved;
        private int sold;
        private int checkedIn;
        private int cancelled;
        private BigDecimal price;
        private BigDecimal revenue;
        private BigDecimal potentialRevenue;
    }
}
