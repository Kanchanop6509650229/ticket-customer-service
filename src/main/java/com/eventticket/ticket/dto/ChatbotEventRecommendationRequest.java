package com.eventticket.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ChatbotEventRecommendationRequest {

    @NotBlank(message = "Query is required")
    @Schema(description = "The user's question or query about event recommendations", example = "Recommend me some concerts")
    private String query;

    @Schema(description = "ID of the user making the query", example = "3")
    private Long userId;

    @Schema(description = "Session ID for tracking conversation context", example = "session123")
    private String sessionId;

    @Schema(description = "Category of events to recommend", example = "Concert")
    private String category;

    @Schema(description = "Minimum price for ticket filtering", example = "1000")
    private Integer minPrice;

    @Schema(description = "Maximum price for ticket filtering", example = "5000")
    private Integer maxPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Start date for event date range filtering", example = "2025-01-01")
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "End date for event date range filtering", example = "2025-12-31")
    private LocalDate dateTo;

    @Schema(description = "City for location filtering", example = "Bangkok")
    private String city;

    @Schema(description = "Country for location filtering", example = "Thailand")
    private String country;

    @Min(value = 1, message = "Number of people must be at least 1")
    @Schema(description = "Number of people in the group", example = "4")
    private Integer numberOfPeople;

    @Schema(description = "Venue name for filtering by venue", example = "Impact Arena")
    private String venueName;

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
