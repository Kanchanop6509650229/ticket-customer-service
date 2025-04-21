package com.eventticket.ticket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String sessionId;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String query;

    @Column(columnDefinition = "TEXT")
    private String response;

    private String eventId;

    private Double confidence;

    @CreationTimestamp
    private LocalDateTime timestamp;
}