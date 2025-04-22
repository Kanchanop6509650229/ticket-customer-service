package com.eventticket.ticket.service;

import com.eventticket.ticket.dto.BookingRequest;
import com.eventticket.ticket.dto.response.BookingResponse;
import com.eventticket.ticket.dto.response.EventStatusResponse;
import com.eventticket.ticket.exception.BusinessException;
import com.eventticket.ticket.exception.ResourceNotFoundException;
import com.eventticket.ticket.model.Booking;
import com.eventticket.ticket.model.Ticket;
import com.eventticket.ticket.repository.BookingRepository;
import com.eventticket.ticket.repository.TicketRepository;
import com.eventticket.ticket.service.client.EventServiceClient;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    // Default constructor for when dependency injection is not used
    public BookingService() {
        // Initialize with null values - these will be properly injected when Spring creates the bean
        this.bookingRepository = null;
        this.ticketRepository = null;
        this.eventServiceClient = null;
        // Note: This constructor should only be used by frameworks or for testing
        // In normal Spring operation, the RequiredArgsConstructor will be used
    }

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final EventServiceClient eventServiceClient;
    private static final int BOOKING_EXPIRATION_MINUTES = 15;

    // Getters and Setters
    public BookingRepository getBookingRepository() {
        return bookingRepository;
    }

    public TicketRepository getTicketRepository() {
        return ticketRepository;
    }

    public EventServiceClient getEventServiceClient() {
        return eventServiceClient;
    }

    public static int getBookingExpirationMinutes() {
        return BOOKING_EXPIRATION_MINUTES;
    }

    public List<BookingResponse> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // Check if event is available
        EventStatusResponse eventStatus = eventServiceClient.getEventStatus(bookingRequest.getEventId());
        if (!"upcoming".equals(eventStatus.getCurrentStatus())) {
            throw new BusinessException("Cannot book tickets for an event that is not upcoming");
        }

        // Find available tickets
        List<Ticket> availableTickets = ticketRepository.findByEventIdAndStatus(
                bookingRequest.getEventId(),
                Ticket.TicketStatus.AVAILABLE
        ).stream()
         .filter(ticket -> ticket.getType().equals(bookingRequest.getTicketType()))
         .limit(bookingRequest.getQuantity())
         .collect(Collectors.toList());

        if (availableTickets.size() < bookingRequest.getQuantity()) {
            throw new BusinessException("Not enough tickets available");
        }

        // Update ticket status
        availableTickets.forEach(ticket -> ticket.setStatus(Ticket.TicketStatus.RESERVED));
        List<Ticket> reservedTickets = ticketRepository.saveAll(availableTickets);

        // Calculate total amount
        BigDecimal totalAmount = reservedTickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(bookingRequest.getUserId());
        booking.setEventId(bookingRequest.getEventId());
        booking.setTickets(reservedTickets);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.BookingStatus.RESERVED);

        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() != Booking.BookingStatus.RESERVED) {
            throw new BusinessException("Booking is not in RESERVED status");
        }

        booking.setStatus(Booking.BookingStatus.PAID);

        // Update ticket status
        booking.getTickets().forEach(ticket -> {
            ticket.setStatus(Ticket.TicketStatus.SOLD);
            ticket.setOwnerId(booking.getUserId());
            ticket.setPurchaseDate(LocalDateTime.now());
        });

        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Update ticket status
        booking.getTickets().forEach(ticket -> {
            ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
            ticket.setOwnerId(null);
            ticket.setPurchaseDate(null);
        });

        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void expireReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(BOOKING_EXPIRATION_MINUTES);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(Booking.BookingStatus.RESERVED, expirationTime);

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);

            // Update ticket status
            booking.getTickets().forEach(ticket -> ticket.setStatus(Ticket.TicketStatus.AVAILABLE));

            bookingRepository.save(booking);
            log.info("Expired booking: {}", booking.getId());
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setEventId(booking.getEventId());

        try {
            response.setEventName(eventServiceClient.getEventDetails(booking.getEventId()).getName());
        } catch (Exception e) {
            log.warn("Could not fetch event name for eventId: {}", booking.getEventId(), e);
            response.setEventName("Unknown Event");
        }

        response.setStatus(booking.getStatus().toString());
        response.setTotalAmount(booking.getTotalAmount());

        List<BookingResponse.TicketInfo> ticketInfos = new ArrayList<>();
        for (Ticket ticket : booking.getTickets()) {
            BookingResponse.TicketInfo ticketInfo = new BookingResponse.TicketInfo();
            ticketInfo.setId(ticket.getId());
            ticketInfo.setType(ticket.getType());
            ticketInfo.setSection(ticket.getSection());
            ticketInfo.setSeatNumber(ticket.getSeatNumber());
            ticketInfo.setPrice(ticket.getPrice());
            ticketInfo.setStatus(ticket.getStatus().toString());
            ticketInfos.add(ticketInfo);
        }

        response.setTickets(ticketInfos);
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}