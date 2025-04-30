package com.eventticket.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Response object for payment information")
public class PaymentResponse {

    @Schema(description = "ID of the payment", example = "1")
    private Long paymentId;

    @Schema(description = "ID of the booking this payment is for", example = "123")
    private Long bookingId;

    @Schema(description = "ID of the user who made the payment", example = "456")
    private Long userId;

    @Schema(description = "ID of the event", example = "1")
    private String eventId;

    @Schema(description = "Total amount paid", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Status of the payment (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)", example = "COMPLETED")
    private String status;

    @Schema(description = "Payment method used (CREDIT_CARD, BANK_TRANSFER, MOBILE_PAYMENT, E_WALLET)", example = "CREDIT_CARD")
    private String method;

    @Schema(description = "Transaction ID from the payment processor", example = "txn_123456789")
    private String transactionId;

    @Schema(description = "Additional payment details", example = "Payment processed via CREDIT_CARD")
    private String paymentDetails;

    @Schema(description = "Date and time when the payment was created", example = "2023-06-15T18:30:00")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
