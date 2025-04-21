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
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long bookingId;

    @NotNull
    private Long userId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String transactionId;

    private String paymentDetails;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    public enum PaymentMethod {
        CREDIT_CARD,
        BANK_TRANSFER,
        MOBILE_PAYMENT,
        E_WALLET
    }
}