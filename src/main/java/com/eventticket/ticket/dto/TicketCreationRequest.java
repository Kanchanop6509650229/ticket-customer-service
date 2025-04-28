package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for ticket creation requests.
 * This is a simplified version of TicketDTO with only the fields needed for creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a ticket")
public class TicketCreationRequest {
    
    @Schema(description = "ID of the event this ticket is for", example = "event123", required = true)
    private String eventId;
    
    @Schema(description = "Type of ticket (VIP, Regular, etc.)", example = "VIP", required = true)
    private String type;
    
    @Schema(description = "Price of the ticket", example = "5000.00", required = true)
    private BigDecimal price;
    
    @Schema(description = "Section where the seat is located", example = "A")
    private String section;
    
    @Schema(description = "Seat number within the section", example = "A1")
    private String seatNumber;
}
