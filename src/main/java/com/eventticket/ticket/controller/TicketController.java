package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.TicketCreationRequest;
import com.eventticket.ticket.dto.TicketDTO;
import com.eventticket.ticket.dto.response.AvailabilityResponse;
import com.eventticket.ticket.dto.response.TicketStatisticsResponse;
import com.eventticket.ticket.model.Ticket;
import com.eventticket.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @Operation(
        summary = "Create one or multiple tickets",
        description = "Create a single ticket or multiple tickets at once. For multiple tickets, send an array of ticket objects.",
        security = @SecurityRequirement(name = "JWT"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Single ticket object or array of ticket objects",
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(oneOf = {TicketCreationRequest.class, TicketCreationRequest[].class}),
                    examples = {
                        @ExampleObject(
                            name = "Single Ticket",
                            summary = "Example of creating a single ticket",
                            value = "{\"eventId\":\"event123\",\"type\":\"VIP\",\"price\":5000.00,\"section\":\"A\",\"seatNumber\":\"A1\"}"
                        ),
                        @ExampleObject(
                            name = "Multiple Tickets",
                            summary = "Example of creating multiple tickets",
                            value = "[{\"eventId\":\"event123\",\"type\":\"VIP\",\"price\":5000.00,\"section\":\"A\",\"seatNumber\":\"A1\"},{\"eventId\":\"event123\",\"type\":\"Regular\",\"price\":2000.00,\"section\":\"B\",\"seatNumber\":\"B1\"}]"
                        )
                    }
                )
            }
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Ticket(s) created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(oneOf = {TicketDTO.class, TicketDTO[].class})
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid ticket data provided"
            )
        }
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<?> createTickets(@Valid @RequestBody Object requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Check if the request is for multiple tickets
        if (requestBody instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> ticketMaps = (List<Map<String, Object>>) requestBody;
                List<TicketDTO> ticketDTOs = ticketMaps.stream()
                        .map(map -> mapper.convertValue(map, TicketDTO.class))
                        .collect(Collectors.toList());

                List<TicketDTO> createdTickets = ticketService.createMultipleTickets(ticketDTOs);

                List<EntityModel<TicketDTO>> ticketResources = createdTickets.stream()
                        .map(ticket -> EntityModel.of(ticket,
                                linkTo(methodOn(TicketController.class).getTicketById(ticket.getId())).withSelfRel(),
                                linkTo(methodOn(TicketController.class).getTicketsByEventId(ticket.getEventId())).withRel("event_tickets")))
                        .collect(Collectors.toList());

                // Use the event ID from the first ticket for the self link
                String eventId = createdTickets.isEmpty() ? "" : createdTickets.get(0).getEventId();
                Link link = linkTo(methodOn(TicketController.class).getTicketsByEventId(eventId)).withSelfRel();
                CollectionModel<EntityModel<TicketDTO>> result = CollectionModel.of(ticketResources, link);

                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid ticket format in the list: " + e.getMessage());
            }
        }
        // Handle single ticket creation
        else if (requestBody instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> ticketMap = (Map<String, Object>) requestBody;
                TicketDTO ticketDTO = mapper.convertValue(ticketMap, TicketDTO.class);

                TicketDTO createdTicket = ticketService.createTicket(ticketDTO);

                EntityModel<TicketDTO> resource = EntityModel.of(createdTicket,
                        linkTo(methodOn(TicketController.class).getTicketById(createdTicket.getId())).withSelfRel(),
                        linkTo(methodOn(TicketController.class).getTicketsByEventId(createdTicket.getEventId())).withRel("event_tickets"));

                return ResponseEntity.ok(resource);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid ticket format: " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Invalid request body format. Expected a ticket object or an array of tickets.");
        }
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

    @GetMapping("/event/{eventId}/statistics")
    @Operation(
        summary = "Get ticket sales statistics for an event",
        description = "Returns detailed statistics about ticket sales for a specific event",
        security = @SecurityRequirement(name = "JWT"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Statistics retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TicketStatisticsResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Event not found"
            )
        }
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<TicketStatisticsResponse> getTicketStatistics(@PathVariable String eventId) {
        TicketStatisticsResponse statistics = ticketService.getTicketStatistics(eventId);
        return ResponseEntity.ok(statistics);
    }
}