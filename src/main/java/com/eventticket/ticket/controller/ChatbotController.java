package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.ChatbotRequest;
import com.eventticket.ticket.dto.response.ChatbotResponse;
import com.eventticket.ticket.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot API", description = "Endpoints for the ticket support chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/booking-help")
    @Operation(summary = "Get help with ticket booking", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ChatbotResponse> getBookingHelp(@Valid @RequestBody ChatbotRequest request) {
        ChatbotResponse response = chatbotService.processBookingHelp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/faq")
    @Operation(summary = "Get answers to frequently asked questions")
    public ResponseEntity<ChatbotResponse> getFaqAnswer(@Valid @RequestBody ChatbotRequest request) {
        ChatbotResponse response = chatbotService.processFaq(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/event-info")
    @Operation(summary = "Get information about events", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ChatbotResponse> getEventInfo(@Valid @RequestBody ChatbotRequest request) {
        // For event info, we'll also use the processBookingHelp method as it can handle event context
        ChatbotResponse response = chatbotService.processBookingHelp(request);
        return ResponseEntity.ok(response);
    }
}