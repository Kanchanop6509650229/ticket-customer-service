package com.eventticket.ticket.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private String eventId;
    private String eventName;
    private List<TicketInfo> tickets;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    
    @Data
    public static class TicketInfo {
        private Long id;
        private String type;
        private String section;
        private String seatNumber;
        private BigDecimal price;
        private String status;
        private String qrCode;
    }
}