package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.ChatbotBookingHelpRequest;
import com.eventticket.ticket.dto.ChatbotEventInfoRequest;
import com.eventticket.ticket.dto.ChatbotFaqRequest;
import com.eventticket.ticket.dto.ChatbotRequest;
import com.eventticket.ticket.dto.response.ChatbotResponse;
import com.eventticket.ticket.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot API", description = "Endpoints for the ticket support chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/booking-help")
    @Operation(summary = "Get help with ticket booking", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ChatbotResponse> getBookingHelp(@Valid @RequestBody ChatbotBookingHelpRequest request) {
        // Convert to ChatbotRequest for service compatibility
        ChatbotRequest chatbotRequest = new ChatbotRequest();
        chatbotRequest.setQuery(request.getQuery());
        chatbotRequest.setUserId(request.getUserId());
        chatbotRequest.setSessionId(request.getSessionId());
        chatbotRequest.setEventId(request.getEventId());

        ChatbotResponse response = chatbotService.processBookingHelp(chatbotRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/faq")
    @Operation(summary = "Get answers to frequently asked questions")
    public ResponseEntity<ChatbotResponse> getFaqAnswer(@Valid @RequestBody ChatbotFaqRequest request) {
        // Convert to ChatbotRequest for service compatibility
        ChatbotRequest chatbotRequest = new ChatbotRequest();
        chatbotRequest.setQuery(request.getQuery());
        chatbotRequest.setUserId(request.getUserId());
        chatbotRequest.setSessionId(request.getSessionId());
        chatbotRequest.setEventId(request.getEventId());

        ChatbotResponse response = chatbotService.processFaq(chatbotRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/event-info")
    @Operation(summary = "Get information about events", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ChatbotResponse> getEventInfo(@Valid @RequestBody ChatbotEventInfoRequest request) {
        // Convert to ChatbotRequest for service compatibility
        ChatbotRequest chatbotRequest = new ChatbotRequest();
        chatbotRequest.setQuery(request.getQuery());
        chatbotRequest.setUserId(request.getUserId());
        chatbotRequest.setSessionId(request.getSessionId());
        chatbotRequest.setEventId(request.getEventId());

        // Use the dedicated event info processor for more detailed event information
        ChatbotResponse response = chatbotService.processEventInfo(chatbotRequest);
        return ResponseEntity.ok(response);
    }
}