package com.eventticket.ticket.service;

import com.eventticket.ticket.dto.TicketDTO;
import com.eventticket.ticket.dto.response.AvailabilityResponse;
import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.exception.ResourceNotFoundException;
import com.eventticket.ticket.model.Ticket;
import com.eventticket.ticket.repository.TicketRepository;
import com.eventticket.ticket.service.client.EventServiceClient;
import com.eventticket.ticket.util.QRCodeGenerator;
import com.eventticket.ticket.util.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventServiceClient eventServiceClient;
    private final QRCodeGenerator qrCodeGenerator;
    private final TicketMapper ticketMapper;

    public List<TicketDTO> getTicketsByEventId(String eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return ticketMapper.toDto(ticket);
    }

    public List<TicketDTO> getTicketsByUserId(Long userId) {
        List<Ticket> tickets = ticketRepository.findByOwnerId(userId);
        return tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    public AvailabilityResponse getAvailabilityByEventId(String eventId) {
        EventResponse eventResponse = eventServiceClient.getEventDetails(eventId);
        List<Ticket> availableTickets = ticketRepository.findByEventIdAndStatus(eventId, Ticket.TicketStatus.AVAILABLE);

        Map<String, Integer> availabilityByType = new HashMap<>();
        for (Ticket ticket : availableTickets) {
            availabilityByType.put(
                ticket.getType(),
                availabilityByType.getOrDefault(ticket.getType(), 0) + 1
            );
        }

        AvailabilityResponse response = new AvailabilityResponse();
        response.setEventId(eventId);
        response.setEventName(eventResponse.getName());
        response.setTotalTickets(ticketRepository.findByEventId(eventId).size());
        response.setSoldTickets((int) ticketRepository.countByEventIdAndStatus(eventId, Ticket.TicketStatus.SOLD));
        response.setAvailableTickets(availableTickets.size());
        response.setAvailabilityByType(availabilityByType);

        return response;
    }

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket.setStatus(Ticket.TicketStatus.AVAILABLE);

        // Generate QR code for the ticket
        String qrCodeData = UUID.randomUUID().toString();
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrCodeData);
        ticket.setQrCode(qrCodeBase64);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDto(savedTicket);
    }

    @Transactional
    public List<Ticket> createTicketsForEvent(String eventId, String ticketType, int quantity, BigDecimal price) {
        List<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setEventId(eventId);
            ticket.setType(ticketType);
            ticket.setPrice(price);
            ticket.setStatus(Ticket.TicketStatus.AVAILABLE);

            // Generate random seat information
            ticket.setSection(generateRandomSection());
            ticket.setSeatNumber(generateSeatNumber(i));

            // Generate QR code
            String qrCodeData = UUID.randomUUID().toString();
            String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrCodeData);
            ticket.setQrCode(qrCodeBase64);

            tickets.add(ticket);
        }

        return ticketRepository.saveAll(tickets);
    }

    @Transactional
    public TicketDTO updateTicketStatus(Long id, Ticket.TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setStatus(status);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDto(updatedTicket);
    }

    @Transactional
    public TicketDTO assignTicketToUser(Long id, Long userId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setOwnerId(userId);
        ticket.setStatus(Ticket.TicketStatus.SOLD);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDto(updatedTicket);
    }

    private String generateRandomSection() {
        String[] sections = {"A", "B", "C", "D", "VIP"};
        return sections[new Random().nextInt(sections.length)];
    }

    private String generateSeatNumber(int index) {
        return String.format("%s%d", (char)('A' + (index / 20)), (index % 20) + 1);
    }

    @Transactional
    public List<TicketDTO> createMultipleTickets(List<TicketDTO> ticketDTOs) {
        List<Ticket> tickets = new ArrayList<>();

        for (TicketDTO ticketDTO : ticketDTOs) {
            Ticket ticket = ticketMapper.toEntity(ticketDTO);
            ticket.setStatus(Ticket.TicketStatus.AVAILABLE);

            // Generate QR code for the ticket
            String qrCodeData = UUID.randomUUID().toString();
            String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrCodeData);
            ticket.setQrCode(qrCodeBase64);

            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
        return savedTickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }
}