package com.eventticket.ticket.service;

import com.eventticket.ticket.dto.ChatbotRequest;
import com.eventticket.ticket.dto.response.ChatbotResponse;
import com.eventticket.ticket.dto.response.EventResponse;
import com.eventticket.ticket.dto.response.SearchEventResponse;
import com.eventticket.ticket.model.ChatHistory;
import com.eventticket.ticket.repository.ChatHistoryRepository;
import com.eventticket.ticket.service.client.EventServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final EventServiceClient eventServiceClient;
    @Qualifier("deepSeekWebClient")
    private final WebClient deepSeekWebClient;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.model}")
    private String modelName;

    public ChatbotResponse processBookingHelp(ChatbotRequest request) {
        // Fetch event details if available
        EventResponse eventDetails = null;
        if (request.getEventId() != null && !request.getEventId().isEmpty()) {
            try {
                eventDetails = eventServiceClient.getEventDetails(request.getEventId());
            } catch (Exception e) {
                log.warn("Could not fetch event details for eventId: {}", request.getEventId(), e);
            }
        }

        // Build prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful ticket booking assistant for an event/concert ticketing platform. ");

        if (eventDetails != null) {
            prompt.append("Context: The user is asking about the event '")
                  .append(eventDetails.getName())
                  .append("' which is a ")
                  .append(eventDetails.getCategory())
                  .append(" happening on ")
                  .append(eventDetails.getDate())
                  .append(" at ")
                  .append(eventDetails.getVenue())
                  .append(".");
        }

        prompt.append("\n\nUser question: ").append(request.getQuery());

        // Get response from LLM
        ChatbotResponse response = callDeepSeek(prompt.toString(), request);

        // Save chat history
        saveChatHistory(request, response);

        return response;
    }

    public ChatbotResponse processEventInfo(ChatbotRequest request) {
        // Fetch event details if available
        EventResponse eventDetails = null;
        if (request.getEventId() != null && !request.getEventId().isEmpty()) {
            try {
                eventDetails = eventServiceClient.getEventDetails(request.getEventId());
            } catch (Exception e) {
                log.warn("Could not fetch event details for eventId: {}", request.getEventId(), e);
            }
        }

        // Build prompt with focus on event information
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an event information specialist for a ticketing platform. ");
        prompt.append("Focus on providing detailed information about the event, such as artists, venue details, ");
        prompt.append("event schedule, special requirements, and what attendees can expect. ");
        prompt.append("Do not focus on the booking process unless specifically asked. ");

        if (eventDetails != null) {
            prompt.append("Context: The user is asking about the event '")
                  .append(eventDetails.getName())
                  .append("' which is a ")
                  .append(eventDetails.getCategory())
                  .append(" happening on ")
                  .append(eventDetails.getDate())
                  .append(" at ")
                  .append(eventDetails.getVenue())
                  .append(".");

            // Add more detailed event information if available
            if (eventDetails.getDescription() != null && !eventDetails.getDescription().isEmpty()) {
                prompt.append(" Event description: ").append(eventDetails.getDescription());
            }

            if (eventDetails.getArtists() != null && !eventDetails.getArtists().isEmpty()) {
                prompt.append(" Featured artists: ").append(String.join(", ", eventDetails.getArtists()));
            }

            if (eventDetails.getTime() != null) {
                prompt.append(" Start time: ").append(eventDetails.getTime());
            }

            if (eventDetails.getDuration() != null) {
                prompt.append(" Duration: ").append(eventDetails.getDuration()).append(" minutes");
            }
        }

        prompt.append("\n\nUser question: ").append(request.getQuery());

        // Get response from LLM
        ChatbotResponse response = callDeepSeekForEventInfo(prompt.toString(), request);

        // Save chat history
        saveChatHistory(request, response);

        return response;
    }

    public ChatbotResponse processFaq(ChatbotRequest request) {
        // Predefined FAQs with responses
        List<Map<String, String>> faqs = new ArrayList<>();
        faqs.add(Map.of(
            "question", "มีส่วนลดสำหรับการจองเป็นกลุ่มหรือไม่?",
            "answer", "สำหรับการจองตั๋วเป็นกลุ่ม 10 คนขึ้นไปจะได้รับส่วนลด 10% โดยต้องทำการจองผ่านช่องทางการขายกลุ่มโดยเฉพาะ"
        ));
        faqs.add(Map.of(
            "question", "ต้องจองล่วงหน้านานแค่ไหนสำหรับกลุ่มใหญ่?",
            "answer", "แนะนำให้จองล่วงหน้าอย่างน้อย 2 สัปดาห์สำหรับกลุ่มใหญ่ เพื่อให้มั่นใจว่าจะมีที่นั่งที่เพียงพอและอยู่ในโซนเดียวกัน"
        ));
        faqs.add(Map.of(
            "question", "มีนโยบายการคืนเงินอย่างไร?",
            "answer", "สามารถขอคืนเงินได้หากยกเลิกการจองล่วงหน้าอย่างน้อย 7 วันก่อนวันจัดงาน โดยจะได้รับเงินคืน 80% ของมูลค่าตั๋ว"
        ));
        faqs.add(Map.of(
            "question", "ต้องใช้เอกสารอะไรบ้างในการเข้างาน?",
            "answer", "ต้องแสดง QR Code ที่ได้รับหลังจากการจองตั๋วและบัตรประชาชนหรือพาสปอร์ตที่มีชื่อตรงกับการจอง"
        ));

        // Find matching FAQ
        String matchedAnswer = null;
        List<Map<String, String>> relatedFaqs = new ArrayList<>();

        for (Map<String, String> faq : faqs) {
            if (faq.get("question").toLowerCase().contains(request.getQuery().toLowerCase()) ||
                request.getQuery().toLowerCase().contains(faq.get("question").toLowerCase().substring(0, Math.min(faq.get("question").length(), 10)))) {
                matchedAnswer = faq.get("answer");
            } else if (!faq.get("question").equals(request.getQuery())) {
                relatedFaqs.add(faq);
            }
        }

        // If no direct match, use LLM
        if (matchedAnswer == null) {
            return processBookingHelp(request);
        }

        // Build response
        ChatbotResponse response = new ChatbotResponse();
        response.setAnswer(matchedAnswer);

        // Add related FAQs (max 2)
        if (!relatedFaqs.isEmpty()) {
            Collections.shuffle(relatedFaqs);
            List<ChatbotResponse.FAQ> relatedFaqList = new ArrayList<>();
            for (int i = 0; i < Math.min(2, relatedFaqs.size()); i++) {
                ChatbotResponse.FAQ faq = new ChatbotResponse.FAQ();
                faq.setQuestion(relatedFaqs.get(i).get("question"));
                faq.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
                relatedFaqList.add(faq);
            }
            response.setRelatedFaq(relatedFaqList);
        }

        response.setConfidence(0.95);

        // Save chat history
        saveChatHistory(request, response);

        return response;
    }

    private ChatbotResponse callDeepSeek(String prompt, ChatbotRequest request) {
        // Note: request parameter is kept for future use (e.g., personalization based on user data)
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are a helpful event ticketing assistant."));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 300);

            String jsonResponse = deepSeekWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(jsonResponse);
            String content = responseJson.path("choices").path(0).path("message").path("content").asText();

            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(content);
            response.setConfidence(0.85);

            // Get related FAQs
            List<ChatbotResponse.FAQ> relatedFaqs = new ArrayList<>();
            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("ต้องการดูวิธีชำระเงินสำหรับการจองตั๋ว");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("มีนโยบายการคืนเงินอย่างไร");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq2);

            response.setRelatedFaq(relatedFaqs);

            return response;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer("ขออภัย ระบบขัดข้องชั่วคราว กรุณาลองใหม่ภายหลังหรือติดต่อฝ่ายบริการลูกค้าที่ support@eventticket.com");
            response.setConfidence(0.5);
            return response;
        }
    }

    private ChatbotResponse callDeepSeekForEventInfo(String prompt, ChatbotRequest request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are an event information specialist providing detailed information about events."));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 300);

            String jsonResponse = deepSeekWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(jsonResponse);
            String content = responseJson.path("choices").path(0).path("message").path("content").asText();

            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(content);
            response.setConfidence(0.9); // Higher confidence for event info

            // Event-specific related FAQs
            List<ChatbotResponse.FAQ> relatedFaqs = new ArrayList<>();

            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("มีที่จอดรถที่สถานที่จัดงานหรือไม่");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("สามารถนำอาหารและเครื่องดื่มเข้างานได้หรือไม่");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq2);

            ChatbotResponse.FAQ faq3 = new ChatbotResponse.FAQ();
            faq3.setQuestion("มีการถ่ายทอดสดงานนี้หรือไม่");
            faq3.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq3);

            // Randomly select 2 FAQs to show
            Collections.shuffle(relatedFaqs);
            relatedFaqs = relatedFaqs.subList(0, Math.min(2, relatedFaqs.size()));

            response.setRelatedFaq(relatedFaqs);

            return response;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for event info", e);

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer("ขออภัย ระบบขัดข้องชั่วคราว กรุณาลองใหม่ภายหลังหรือติดต่อฝ่ายบริการลูกค้าที่ support@eventticket.com");
            response.setConfidence(0.5);
            return response;
        }
    }

    private void saveChatHistory(ChatbotRequest request, ChatbotResponse response) {
        // Check if repository is initialized
        if (chatHistoryRepository == null) {
            // Log warning when repository is not available
            System.out.println("Warning: chatHistoryRepository is null, cannot save chat history");
            return;
        }

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(request.getUserId());
        chatHistory.setSessionId(request.getSessionId());
        chatHistory.setQuery(request.getQuery());
        chatHistory.setResponse(response.getAnswer());
        chatHistory.setEventId(request.getEventId());
        chatHistory.setConfidence(response.getConfidence());

        chatHistoryRepository.save(chatHistory);
    }
}