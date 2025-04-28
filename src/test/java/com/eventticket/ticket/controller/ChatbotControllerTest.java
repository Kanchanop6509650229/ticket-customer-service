package com.eventticket.ticket.controller;

import com.eventticket.ticket.dto.ChatbotRequest;
import com.eventticket.ticket.dto.response.ChatbotResponse;
import com.eventticket.ticket.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatbotController.class)
public class ChatbotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatbotService chatbotService;

    @Test
    public void testBookingHelpEndpoint() throws Exception {
        // Prepare test data
        ChatbotRequest request = new ChatbotRequest();
        request.setQuery("How do I book tickets?");
        request.setEventId("1");

        ChatbotResponse mockResponse = new ChatbotResponse();
        mockResponse.setAnswer("To book tickets, go to the event page and select the tickets you want to purchase.");
        mockResponse.setConfidence(0.85);

        // Mock service behavior
        when(chatbotService.processBookingHelp(any(ChatbotRequest.class))).thenReturn(mockResponse);

        // Perform the test
        mockMvc.perform(post("/api/chatbot/booking-help")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("To book tickets, go to the event page and select the tickets you want to purchase."))
                .andExpect(jsonPath("$.confidence").value(0.85));
    }

    @Test
    public void testEventInfoEndpoint() throws Exception {
        // Prepare test data
        ChatbotRequest request = new ChatbotRequest();
        request.setQuery("Tell me about the BNK48 concert");
        request.setEventId("1");

        ChatbotResponse mockResponse = new ChatbotResponse();
        mockResponse.setAnswer("The BNK48 concert will be held at Impact Arena on June 15, 2025. It features all members of the popular idol group.");
        mockResponse.setConfidence(0.9);

        // Mock service behavior - this should use processEventInfo now
        when(chatbotService.processEventInfo(any(ChatbotRequest.class))).thenReturn(mockResponse);

        // Perform the test
        mockMvc.perform(post("/api/chatbot/event-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("The BNK48 concert will be held at Impact Arena on June 15, 2025. It features all members of the popular idol group."))
                .andExpect(jsonPath("$.confidence").value(0.9));
    }

    @Test
    public void testFaqEndpoint() throws Exception {
        // Prepare test data
        ChatbotRequest request = new ChatbotRequest();
        request.setQuery("What is your refund policy?");

        ChatbotResponse mockResponse = new ChatbotResponse();
        mockResponse.setAnswer("You can get a refund if you cancel at least 7 days before the event. You will receive 80% of the ticket price back.");
        mockResponse.setConfidence(0.95);

        // Mock service behavior
        when(chatbotService.processFaq(any(ChatbotRequest.class))).thenReturn(mockResponse);

        // Perform the test
        mockMvc.perform(post("/api/chatbot/faq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("You can get a refund if you cancel at least 7 days before the event. You will receive 80% of the ticket price back."))
                .andExpect(jsonPath("$.confidence").value(0.95));
    }
}
