package com.eventticket.ticket.dto;

import com.eventticket.ticket.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    
    @NotBlank(message = "Event ID is required")
    private String eventId;
    
    @NotBlank(message = "Ticket type is required")
    private String type;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String section;
    
    private String seatNumber;
    
    private Ticket.TicketStatus status;
    
    private Long ownerId;
    
    private LocalDateTime purchaseDate;
    
    private String qrCode;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Additional fields for returning to client
    private String eventName;
}