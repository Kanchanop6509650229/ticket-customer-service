package com.eventticket.ticket.repository;

import com.eventticket.ticket.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    
    List<Booking> findByEventId(String eventId);
    
    List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status);
    
    List<Booking> findByEventIdAndStatus(String eventId, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :expirationTime")
    List<Booking> findExpiredBookings(@Param("status") Booking.BookingStatus status, @Param("expirationTime") LocalDateTime expirationTime);
}