package com.eventticket.ticket.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Ticket type is required")
    private String ticketType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String note;
}