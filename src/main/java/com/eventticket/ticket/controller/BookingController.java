package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.BookingRequest;
import com.eventticket.ticket.dto.response.BookingResponse;
import com.eventticket.ticket.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking API", description = "Endpoints for managing ticket bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get bookings for a user", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<BookingResponse>>> getBookingsByUserId(
            @RequestParam Long userId) {
        
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        
        List<EntityModel<BookingResponse>> bookingResources = bookings.stream()
                .map(booking -> EntityModel.of(booking, 
                        linkTo(methodOn(BookingController.class).getBookingById(booking.getBookingId())).withSelfRel()))
                .collect(Collectors.toList());
        
        Link link = linkTo(methodOn(BookingController.class).getBookingsByUserId(userId)).withSelfRel();
        CollectionModel<EntityModel<BookingResponse>> result = CollectionModel.of(bookingResources, link);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        
        EntityModel<BookingResponse> resource = EntityModel.of(booking,
                linkTo(methodOn(BookingController.class).getBookingById(id)).withSelfRel(),
                linkTo(methodOn(BookingController.class).getBookingsByUserId(booking.getUserId())).withRel("user_bookings"));
        
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @Operation(summary = "Create a new booking", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse createdBooking = bookingService.createBooking(bookingRequest);
        
        EntityModel<BookingResponse> resource = EntityModel.of(createdBooking,
                linkTo(methodOn(BookingController.class).getBookingById(createdBooking.getBookingId())).withSelfRel(),
                linkTo(methodOn(BookingController.class).confirmBooking(createdBooking.getBookingId())).withRel("confirm"),
                linkTo(methodOn(BookingController.class).cancelBooking(createdBooking.getBookingId())).withRel("cancel"));
        
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<BookingResponse>> confirmBooking(@PathVariable Long id) {
        BookingResponse confirmedBooking = bookingService.confirmBooking(id);
        
        EntityModel<BookingResponse> resource = EntityModel.of(confirmedBooking,
                linkTo(methodOn(BookingController.class).getBookingById(confirmedBooking.getBookingId())).withSelfRel());
        
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<BookingResponse>> cancelBooking(@PathVariable Long id) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(id);
        
        EntityModel<BookingResponse> resource = EntityModel.of(cancelledBooking,
                linkTo(methodOn(BookingController.class).getBookingById(cancelledBooking.getBookingId())).withSelfRel());
        
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Process payment for a booking", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EntityModel<BookingResponse>> processPayment(
            @PathVariable Long id,
            @RequestBody String paymentId) {
        
        // This would typically call the payment service, but for simplicity, we're just confirming the booking
        BookingResponse confirmedBooking = bookingService.confirmBooking(id);
        
        EntityModel<BookingResponse> resource = EntityModel.of(confirmedBooking,
                linkTo(methodOn(BookingController.class).getBookingById(confirmedBooking.getBookingId())).withSelfRel());
        
        return ResponseEntity.ok(resource);
    }
}