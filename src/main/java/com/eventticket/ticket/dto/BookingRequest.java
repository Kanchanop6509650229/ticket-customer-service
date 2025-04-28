package com.eventticket.ticket.dto;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class BookingRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user making the booking", example = "3")
    private Long userId;

    @NotBlank(message = "Event ID is required")
    @Schema(description = "ID of the event being booked", example = "1")
    private String eventId;

    @NotBlank(message = "Ticket type is required")
    @Schema(description = "Type of ticket being booked", example = "VIP")
    private String ticketType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Number of tickets to book", example = "2")
    private Integer quantity;

    @Schema(description = "Optional note for the booking", example = "Please seat us together if possible")
    private String note;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}