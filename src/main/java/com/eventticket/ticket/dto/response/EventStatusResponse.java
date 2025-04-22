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

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<StatusChange> getChanges() {
        return changes;
    }

    public void setChanges(List<StatusChange> changes) {
        this.changes = changes;
    }

    public Map<String, Boolean> getNotificationsStatus() {
        return notificationsStatus;
    }

    public void setNotificationsStatus(Map<String, Boolean> notificationsStatus) {
        this.notificationsStatus = notificationsStatus;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusChange {
        private LocalDateTime timestamp;
        private String field;
        private String oldValue;
        private String newValue;
        private String reason;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}