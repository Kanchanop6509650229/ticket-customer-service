package com.eventticket.ticket.dto;

import com.eventticket.ticket.model.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Unique identifier for the ticket", example = "1")
    private Long id;

    @NotBlank(message = "Event ID is required")
    @Schema(description = "ID of the event this ticket is for", example = "1")
    private String eventId;

    @NotBlank(message = "Ticket type is required")
    @Schema(description = "Type of ticket (VIP, Regular, etc.)", example = "VIP")
    private String type;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Price of the ticket", example = "5000.00")
    private BigDecimal price;

    @Schema(description = "Section where the seat is located", example = "A")
    private String section;

    @Schema(description = "Seat number within the section", example = "A1")
    private String seatNumber;

    @Schema(description = "Current status of the ticket", example = "AVAILABLE")
    private Ticket.TicketStatus status;

    @Schema(description = "ID of the user who owns this ticket", example = "3")
    private Long ownerId;

    @Schema(description = "Date and time when the ticket was purchased", example = "2025-06-01T10:30:00")
    private LocalDateTime purchaseDate;

    @Schema(description = "QR code for ticket validation", example = "https://example.com/qr/ticket1")
    private String qrCode;

    @Schema(description = "Date and time when the ticket was created", example = "2025-05-15T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the ticket was last updated", example = "2025-05-15T09:00:00")
    private LocalDateTime updatedAt;

    // Additional fields for returning to client
    @Schema(description = "Name of the event this ticket is for", example = "BNK48 Concert 2025")
    private String eventName;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Ticket.TicketStatus getStatus() {
        return status;
    }

    public void setStatus(Ticket.TicketStatus status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}