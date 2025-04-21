package com.eventticket.ticket.repository;

import com.eventticket.ticket.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserId(Long userId);
    
    List<ChatHistory> findBySessionId(String sessionId);
    
    List<ChatHistory> findByEventId(String eventId);
    
    List<ChatHistory> findTop10ByUserIdOrderByTimestampDesc(Long userId);
    
    List<ChatHistory> findTop10BySessionIdOrderByTimestampDesc(String sessionId);
}