package com.eventticket.ticket.service;

import com.eventticket.ticket.dto.TicketDTO;
import com.eventticket.ticket.dto.response.AvailabilityResponse;
import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.dto.response.TicketStatisticsResponse;
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

    /**
     * Get detailed ticket statistics for an event
     *
     * @param eventId The ID of the event to get statistics for
     * @return TicketStatisticsResponse with detailed statistics
     */
    public TicketStatisticsResponse getTicketStatistics(String eventId) {
        log.info("Generating ticket statistics for event: {}", eventId);

        // Get event details
        EventResponse eventResponse = eventServiceClient.getEventDetails(eventId);

        // Get all tickets for the event
        List<Ticket> allTickets = ticketRepository.findByEventId(eventId);

        if (allTickets.isEmpty()) {
            log.warn("No tickets found for event: {}", eventId);
            return TicketStatisticsResponse.builder()
                    .eventId(eventId)
                    .eventName(eventResponse.getName())
                    .totalTickets(0)
                    .availableTickets(0)
                    .reservedTickets(0)
                    .soldTickets(0)
                    .checkedInTickets(0)
                    .cancelledTickets(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .potentialRevenue(BigDecimal.ZERO)
                    .statisticsByType(new HashMap<>())
                    .build();
        }

        // Count tickets by status
        int availableCount = 0;
        int reservedCount = 0;
        int soldCount = 0;
        int checkedInCount = 0;
        int cancelledCount = 0;

        // Calculate revenue
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal potentialRevenue = BigDecimal.ZERO;

        // Group tickets by type for detailed statistics
        Map<String, List<Ticket>> ticketsByType = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getType));

        // Create statistics by type
        Map<String, TicketStatisticsResponse.TypeStatistics> statisticsByType = new HashMap<>();

        // Process all tickets
        for (Ticket ticket : allTickets) {
            // Add to potential revenue
            potentialRevenue = potentialRevenue.add(ticket.getPrice());

            // Count by status
            switch (ticket.getStatus()) {
                case AVAILABLE:
                    availableCount++;
                    break;
                case RESERVED:
                    reservedCount++;
                    break;
                case SOLD:
                    soldCount++;
                    totalRevenue = totalRevenue.add(ticket.getPrice());
                    break;
                case CHECKED_IN:
                    checkedInCount++;
                    totalRevenue = totalRevenue.add(ticket.getPrice());
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        // Process statistics by ticket type
        for (Map.Entry<String, List<Ticket>> entry : ticketsByType.entrySet()) {
            String type = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            // Count by status for this type
            int typeAvailable = 0;
            int typeReserved = 0;
            int typeSold = 0;
            int typeCheckedIn = 0;
            int typeCancelled = 0;

            // Calculate revenue for this type
            BigDecimal typeRevenue = BigDecimal.ZERO;
            BigDecimal typePotentialRevenue = BigDecimal.ZERO;

            // Get price (assuming all tickets of the same type have the same price)
            BigDecimal price = tickets.get(0).getPrice();

            // Process tickets of this type
            for (Ticket ticket : tickets) {
                // Add to potential revenue
                typePotentialRevenue = typePotentialRevenue.add(ticket.getPrice());

                // Count by status
                switch (ticket.getStatus()) {
                    case AVAILABLE:
                        typeAvailable++;
                        break;
                    case RESERVED:
                        typeReserved++;
                        break;
                    case SOLD:
                        typeSold++;
                        typeRevenue = typeRevenue.add(ticket.getPrice());
                        break;
                    case CHECKED_IN:
                        typeCheckedIn++;
                        typeRevenue = typeRevenue.add(ticket.getPrice());
                        break;
                    case CANCELLED:
                        typeCancelled++;
                        break;
                }
            }

            // Create type statistics
            TicketStatisticsResponse.TypeStatistics typeStats = TicketStatisticsResponse.TypeStatistics.builder()
                    .type(type)
                    .total(tickets.size())
                    .available(typeAvailable)
                    .reserved(typeReserved)
                    .sold(typeSold)
                    .checkedIn(typeCheckedIn)
                    .cancelled(typeCancelled)
                    .price(price)
                    .revenue(typeRevenue)
                    .potentialRevenue(typePotentialRevenue)
                    .build();

            statisticsByType.put(type, typeStats);
        }

        // Build and return the response
        return TicketStatisticsResponse.builder()
                .eventId(eventId)
                .eventName(eventResponse.getName())
                .totalTickets(allTickets.size())
                .availableTickets(availableCount)
                .reservedTickets(reservedCount)
                .soldTickets(soldCount)
                .checkedInTickets(checkedInCount)
                .cancelledTickets(cancelledCount)
                .totalRevenue(totalRevenue)
                .potentialRevenue(potentialRevenue)
                .statisticsByType(statisticsByType)
                .build();
    }
}