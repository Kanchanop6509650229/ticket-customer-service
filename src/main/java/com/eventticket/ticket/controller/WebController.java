package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.response.AvailabilityResponse;
import com.eventticket.ticket.dto.response.BookingResponse;
import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.model.User;
import com.eventticket.ticket.service.BookingService;
import com.eventticket.ticket.service.TicketService;
import com.eventticket.ticket.service.UserService;
import com.eventticket.ticket.service.client.EventServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller for web pages in the ticket customer service application.
 * This controller serves Thymeleaf templates for the web client.
 */
@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebController {

    private final EventServiceClient eventServiceClient;
    private final TicketService ticketService;
    private final BookingService bookingService;
    private final UserService userService;

    /**
     * Home page showing available events
     */
    @GetMapping
    public String home(Model model) {
        // For demo purposes, we'll use event IDs 1-3 which are mocked in the EventServiceClient
        EventResponse event1 = eventServiceClient.getEventDetails("1");
        EventResponse event2 = eventServiceClient.getEventDetails("2");
        EventResponse event3 = eventServiceClient.getEventDetails("3");
        
        model.addAttribute("events", List.of(event1, event2, event3));
        return "home";
    }

    /**
     * Event details page
     */
    @GetMapping("/events/{eventId}")
    public String eventDetails(@PathVariable String eventId, Model model) {
        EventResponse event = eventServiceClient.getEventDetails(eventId);
        AvailabilityResponse availability = ticketService.getAvailabilityByEventId(eventId);
        
        model.addAttribute("event", event);
        model.addAttribute("availability", availability);
        return "event-details";
    }

    /**
     * Booking page for an event
     */
    @GetMapping("/events/{eventId}/book")
    public String bookEvent(@PathVariable String eventId, Model model) {
        EventResponse event = eventServiceClient.getEventDetails(eventId);
        AvailabilityResponse availability = ticketService.getAvailabilityByEventId(eventId);
        
        // For demo purposes, we'll use the first user
        User user = userService.getUserById(3L); // Regular user
        
        model.addAttribute("event", event);
        model.addAttribute("availability", availability);
        model.addAttribute("user", user);
        return "booking";
    }

    /**
     * User bookings page
     */
    @GetMapping("/user/bookings")
    public String userBookings(Model model) {
        // For demo purposes, we'll use the first user
        Long userId = 3L; // Regular user
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        User user = userService.getUserById(userId);
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);
        return "user-bookings";
    }

    /**
     * Booking details page
     */
    @GetMapping("/bookings/{bookingId}")
    public String bookingDetails(@PathVariable Long bookingId, Model model) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        EventResponse event = eventServiceClient.getEventDetails(booking.getEventId());
        
        model.addAttribute("booking", booking);
        model.addAttribute("event", event);
        return "booking-details";
    }

    /**
     * Chatbot page
     */
    @GetMapping("/chatbot")
    public String chatbot(Model model) {
        // For demo purposes, we'll use the first user
        User user = userService.getUserById(3L); // Regular user
        
        model.addAttribute("user", user);
        return "chatbot";
    }
}
