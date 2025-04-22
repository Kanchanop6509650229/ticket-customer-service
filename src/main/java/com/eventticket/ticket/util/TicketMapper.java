package com.eventticket.ticket.util;

import com.eventticket.ticket.dto.TicketDTO;
import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.model.Ticket;
import com.eventticket.ticket.service.client.EventServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketMapper {

    private static final Logger log = LoggerFactory.getLogger(TicketMapper.class);

    // Default constructor for when dependency injection is not used
    public TicketMapper() {
        // Initialize with null value - this will be properly injected when Spring creates the bean
        this.eventServiceClient = null;
        // Note: This constructor should only be used by frameworks or for testing
        // In normal Spring operation, the RequiredArgsConstructor will be used
    }

    private final EventServiceClient eventServiceClient;

    public TicketDTO toDto(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setEventId(ticket.getEventId());
        dto.setType(ticket.getType());
        dto.setPrice(ticket.getPrice());
        dto.setSection(ticket.getSection());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setStatus(ticket.getStatus());
        dto.setOwnerId(ticket.getOwnerId());
        dto.setPurchaseDate(ticket.getPurchaseDate());
        dto.setQrCode(ticket.getQrCode());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        // Add event name if possible
        try {
            // Check if eventServiceClient is initialized
            if (eventServiceClient != null) {
                EventResponse eventResponse = eventServiceClient.getEventDetails(ticket.getEventId());
                dto.setEventName(eventResponse.getName());
            } else {
                log.warn("EventServiceClient is not initialized");
                dto.setEventName("Unknown Event");
            }
        } catch (Exception e) {
            log.warn("Could not fetch event name for eventId: {}", ticket.getEventId(), e);
            dto.setEventName("Unknown Event");
        }

        return dto;
    }

    public Ticket toEntity(TicketDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setId(dto.getId());
        ticket.setEventId(dto.getEventId());
        ticket.setType(dto.getType());
        ticket.setPrice(dto.getPrice());
        ticket.setSection(dto.getSection());
        ticket.setSeatNumber(dto.getSeatNumber());
        ticket.setStatus(dto.getStatus());
        ticket.setOwnerId(dto.getOwnerId());
        ticket.setPurchaseDate(dto.getPurchaseDate());
        ticket.setQrCode(dto.getQrCode());

        return ticket;
    }
}