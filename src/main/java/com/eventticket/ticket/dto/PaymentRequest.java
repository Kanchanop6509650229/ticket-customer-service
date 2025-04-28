package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Payment information for processing a booking payment",
        example = "{\"paymentId\": \"txn_123456789\", \"method\": \"CREDIT_CARD\"}")
public class PaymentRequest {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "Transaction ID from the payment processor", example = "txn_123456789")
    private String paymentId;

    @Schema(description = "Payment method used", example = "CREDIT_CARD")
    private String method;

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
