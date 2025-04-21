package com.eventticket.ticket.repository;

import com.eventticket.ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventId(String eventId);
    
    List<Ticket> findByOwnerId(Long ownerId);
    
    List<Ticket> findByEventIdAndStatus(String eventId, Ticket.TicketStatus status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.eventId = :eventId AND t.status = :status")
    long countByEventIdAndStatus(@Param("eventId") String eventId, @Param("status") Ticket.TicketStatus status);
    
    @Query("SELECT t.type, COUNT(t) FROM Ticket t WHERE t.eventId = :eventId AND t.status = :status GROUP BY t.type")
    List<Object[]> countByEventIdAndStatusGroupByType(@Param("eventId") String eventId, @Param("status") Ticket.TicketStatus status);
}