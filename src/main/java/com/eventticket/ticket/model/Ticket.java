package com.eventticket.ticket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String eventId;

    @NotBlank
    private String type;

    @NotNull
    @Positive
    private BigDecimal price;

    private String section;

    private String seatNumber;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private Long ownerId;

    private LocalDateTime purchaseDate;

    private String qrCode;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TicketStatus {
        AVAILABLE,
        RESERVED,
        SOLD,
        CHECKED_IN,
        CANCELLED
    }
}