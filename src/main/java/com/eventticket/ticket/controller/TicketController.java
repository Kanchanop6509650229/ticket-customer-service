package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.TicketDTO;
import com.eventticket.ticket.dto.response.AvailabilityResponse;
import com.eventticket.ticket.model.Ticket;
import com.eventticket.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Ticket API", description = "Endpoints for managing tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all tickets for an event", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<CollectionModel<EntityModel<TicketDTO>>> getTicketsByEventId(@PathVariable String eventId) {
        List<TicketDTO> tickets = ticketService.getTicketsByEventId(eventId);

        List<EntityModel<TicketDTO>> ticketResources = tickets.stream()
                .map(ticket -> EntityModel.of(ticket,
                        linkTo(methodOn(TicketController.class).getTicketById(ticket.getId())).withSelfRel(),
                        linkTo(methodOn(TicketController.class).getTicketsByEventId(eventId)).withRel("event_tickets")))
                .collect(Collectors.toList());

        Link link = linkTo(methodOn(TicketController.class).getTicketsByEventId(eventId)).withSelfRel();
        CollectionModel<EntityModel<TicketDTO>> result = CollectionModel.of(ticketResources, link);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/availability/{eventId}")
    @Operation(summary = "Get ticket availability for an event")
    public ResponseEntity<AvailabilityResponse> getTicketAvailability(@PathVariable String eventId) {
        AvailabilityResponse availabilityResponse = ticketService.getAvailabilityByEventId(eventId);
        return ResponseEntity.ok(availabilityResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<TicketDTO>> getTicketById(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);

        EntityModel<TicketDTO> resource = EntityModel.of(ticket,
                linkTo(methodOn(TicketController.class).getTicketById(id)).withSelfRel(),
                linkTo(methodOn(TicketController.class).getTicketsByEventId(ticket.getEventId())).withRel("event_tickets"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tickets owned by a user", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<CollectionModel<EntityModel<TicketDTO>>> getTicketsByUserId(@PathVariable Long userId) {
        List<TicketDTO> tickets = ticketService.getTicketsByUserId(userId);

        List<EntityModel<TicketDTO>> ticketResources = tickets.stream()
                .map(ticket -> EntityModel.of(ticket,
                        linkTo(methodOn(TicketController.class).getTicketById(ticket.getId())).withSelfRel()))
                .collect(Collectors.toList());

        Link link = linkTo(methodOn(TicketController.class).getTicketsByUserId(userId)).withSelfRel();
        CollectionModel<EntityModel<TicketDTO>> result = CollectionModel.of(ticketResources, link);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create a new ticket", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<TicketDTO>> createTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        TicketDTO createdTicket = ticketService.createTicket(ticketDTO);

        EntityModel<TicketDTO> resource = EntityModel.of(createdTicket,
                linkTo(methodOn(TicketController.class).getTicketById(createdTicket.getId())).withSelfRel(),
                linkTo(methodOn(TicketController.class).getTicketsByEventId(createdTicket.getEventId())).withRel("event_tickets"));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ticket status", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<TicketDTO>> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam Ticket.TicketStatus status) {

        TicketDTO updatedTicket = ticketService.updateTicketStatus(id, status);

        EntityModel<TicketDTO> resource = EntityModel.of(updatedTicket,
                linkTo(methodOn(TicketController.class).getTicketById(updatedTicket.getId())).withSelfRel(),
                linkTo(methodOn(TicketController.class).getTicketsByEventId(updatedTicket.getEventId())).withRel("event_tickets"));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/assign/{userId}")
    @Operation(summary = "Assign ticket to a user", security = @SecurityRequirement(name = "JWT"))
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EntityModel<TicketDTO>> assignTicketToUser(
            @PathVariable Long id,
            @PathVariable Long userId) {

        TicketDTO updatedTicket = ticketService.assignTicketToUser(id, userId);

        EntityModel<TicketDTO> resource = EntityModel.of(updatedTicket,
                linkTo(methodOn(TicketController.class).getTicketById(updatedTicket.getId())).withSelfRel(),
                linkTo(methodOn(TicketController.class).getTicketsByUserId(userId)).withRel("user_tickets"));

        return ResponseEntity.ok(resource);
    }
}