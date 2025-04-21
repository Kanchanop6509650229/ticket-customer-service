package com.eventticket.ticket.service.client;

import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.dto.response.EventStatusResponse;
import com.eventticket.ticket.dto.response.SearchEventResponse;
import com.eventticket.ticket.exception.ServiceCommunicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventServiceClient {

    private final RestTemplate restTemplate;

    @Value("${event.service.url}")
    private String eventServiceUrl;

    @Value("${event.service.api-key}")
    private String apiKey;

    public EventResponse getEventDetails(String eventId) {
        try {
            String url = eventServiceUrl + "/api/events/" + eventId;
            HttpHeaders headers = createHeaders();
            
            ResponseEntity<EventResponse> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    new HttpEntity<>(headers), 
                    EventResponse.class);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error retrieving event details for eventId: {}", eventId, e);
            throw new ServiceCommunicationException("Could not retrieve event details: " + e.getMessage());
        }
    }

    public EventStatusResponse getEventStatus(String eventId) {
        try {
            String url = eventServiceUrl + "/api/events/" + eventId + "/status";
            HttpHeaders headers = createHeaders();
            
            ResponseEntity<EventStatusResponse> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    new HttpEntity<>(headers), 
                    EventStatusResponse.class);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error retrieving event status for eventId: {}", eventId, e);
            throw new ServiceCommunicationException("Could not retrieve event status: " + e.getMessage());
        }
    }

    public SearchEventResponse searchEvents(Map<String, String> queryParams) {
        try {
            String url = eventServiceUrl + "/api/search/events/filter";
            HttpHeaders headers = createHeaders();
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            queryParams.forEach(builder::queryParam);
            
            ResponseEntity<SearchEventResponse> response = restTemplate.exchange(
                    builder.toUriString(), 
                    HttpMethod.GET, 
                    new HttpEntity<>(headers), 
                    SearchEventResponse.class);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error searching events with parameters: {}", queryParams, e);
            throw new ServiceCommunicationException("Could not search events: " + e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }
}