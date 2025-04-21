package com.eventticket.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventStatusResponse {
    private String eventId;
    private String currentStatus;
    private LocalDateTime lastUpdated;
    private List<StatusChange> changes;
    private Map<String, Boolean> notificationsStatus;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusChange {
        private LocalDateTime timestamp;
        private String field;
        private String oldValue;
        private String newValue;
        private String reason;
    }
}