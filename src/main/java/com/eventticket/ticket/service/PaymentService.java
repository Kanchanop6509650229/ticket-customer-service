package com.eventticket.ticket.service;

import com.eventticket.ticket.dto.PaymentRequest;
import com.eventticket.ticket.dto.response.PaymentResponse;
import com.eventticket.ticket.exception.BusinessException;
import com.eventticket.ticket.exception.ResourceNotFoundException;
import com.eventticket.ticket.model.Booking;
import com.eventticket.ticket.model.Payment;
import com.eventticket.ticket.repository.BookingRepository;
import com.eventticket.ticket.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * Process payment for a booking
     *
     * @param bookingId The ID of the booking to process payment for
     * @param paymentRequest The payment request containing payment details
     * @return PaymentResponse with payment details
     */
    @Transactional
    public PaymentResponse processPayment(Long bookingId, PaymentRequest paymentRequest) {
        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Validate booking status - now checking for CONFIRMED status
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BusinessException("Booking is not in CONFIRMED status. Please confirm your booking before making a payment.");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(booking.getUserId());
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(Payment.PaymentStatus.COMPLETED); // In a real system, this would depend on payment gateway response
        payment.setMethod(Payment.PaymentMethod.valueOf(paymentRequest.getMethod()));
        payment.setTransactionId(paymentRequest.getPaymentId());
        payment.setPaymentDetails("Payment processed via " + paymentRequest.getMethod());

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status after payment
        booking.setStatus(Booking.BookingStatus.PAID);
        booking.setPaymentId(savedPayment.getTransactionId());

        // Update ticket statuses
        booking.getTickets().forEach(ticket -> {
            ticket.setStatus(com.eventticket.ticket.model.Ticket.TicketStatus.SOLD);
            ticket.setOwnerId(booking.getUserId());
            ticket.setPurchaseDate(LocalDateTime.now());
        });

        bookingRepository.save(booking);

        // Return payment response
        return mapToPaymentResponse(savedPayment, booking);
    }

    /**
     * Get payment by ID
     *
     * @param id The payment ID
     * @return PaymentResponse with payment details
     */
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + payment.getBookingId()));

        return mapToPaymentResponse(payment, booking);
    }

    /**
     * Get payments by booking ID
     *
     * @param bookingId The booking ID
     * @return List of PaymentResponse objects
     */
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        return payments.stream()
                .map(payment -> mapToPaymentResponse(payment, booking))
                .toList();
    }

    /**
     * Map Payment entity to PaymentResponse DTO
     *
     * @param payment The Payment entity
     * @param booking The associated Booking entity
     * @return PaymentResponse DTO
     */
    private PaymentResponse mapToPaymentResponse(Payment payment, Booking booking) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setBookingId(payment.getBookingId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus().name());
        response.setMethod(payment.getMethod().name());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDetails(payment.getPaymentDetails());
        response.setCreatedAt(payment.getCreatedAt());
        response.setEventId(booking.getEventId());

        return response;
    }
}
