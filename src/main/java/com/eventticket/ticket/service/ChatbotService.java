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

import java.time.LocalDate;
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
                "question", "Are there discounts for group bookings?",
                "answer",
                "For group bookings of 10 people or more, a 10% discount is available. Bookings must be made through the dedicated group sales channel."));
        faqs.add(Map.of(
                "question", "How far in advance should large groups book?",
                "answer",
                "We recommend booking at least 2 weeks in advance for large groups to ensure sufficient seating availability in the same zone."));
        faqs.add(Map.of(
                "question", "What is the refund policy?",
                "answer",
                "Refunds are available for cancellations made at least 7 days before the event date. You will receive 80% of the ticket value."));
        faqs.add(Map.of(
                "question", "What documents are required for entry?",
                "answer",
                "You must present the QR Code received after booking and an ID card or passport with a name matching the booking."));

        // Find matching FAQ
        String matchedAnswer = null;
        List<Map<String, String>> relatedFaqs = new ArrayList<>();

        for (Map<String, String> faq : faqs) {
            if (faq.get("question").toLowerCase().contains(request.getQuery().toLowerCase()) ||
                    request.getQuery().toLowerCase().contains(faq.get("question").toLowerCase().substring(0,
                            Math.min(faq.get("question").length(), 10)))) {
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
        // Note: request parameter is kept for future use (e.g., personalization based
        // on user data)
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
            faq1.setQuestion("How to view payment methods for ticket booking");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("What is the refund policy");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq2);

            response.setRelatedFaq(relatedFaqs);

            return response;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(
                    "Sorry, the system is temporarily unavailable. Please try again later or contact customer service at support@eventticket.com");
            response.setConfidence(0.5);
            return response;
        }
    }

    private ChatbotResponse callDeepSeekForEventInfo(String prompt, ChatbotRequest request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are an event information specialist providing detailed information about events."));
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
            faq1.setQuestion("Is there parking available at the venue");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("Can I bring food and drinks to the event");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq2);

            ChatbotResponse.FAQ faq3 = new ChatbotResponse.FAQ();
            faq3.setQuestion("Will this event be livestreamed");
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
            response.setAnswer(
                    "Sorry, the system is temporarily unavailable. Please try again later or contact customer service at support@eventticket.com");
            response.setConfidence(0.5);
            return response;
        }
    }

    public ChatbotResponse processEventRecommendations(ChatbotRequest request, String category) {
        // For backward compatibility, call the new method with only category filter
        return processEventRecommendationsWithFilters(
            request, category, null, null, null, null, null, null, null, null
        );
    }

    public ChatbotResponse processEventRecommendationsWithFilters(
            ChatbotRequest request,
            String category,
            Integer minPrice,
            Integer maxPrice,
            LocalDate dateFrom,
            LocalDate dateTo,
            String city,
            String country,
            Integer numberOfPeople,
            String venueName) {

        // Create a map to track which filters were successfully applied
        Map<String, Boolean> appliedFilters = new HashMap<>();
        SearchEventResponse searchResponse = null;
        List<String> failedFilters = new ArrayList<>();

        try {
            // First try with just the category filter as it's most likely to work
            if (category != null && !category.isEmpty()) {
                Map<String, String> basicParams = new HashMap<>();
                basicParams.put("category", category);
                basicParams.put("page", "0");
                basicParams.put("size", "5");
                basicParams.put("sortBy", "date");
                basicParams.put("sortDirection", "asc");

                try {
                    searchResponse = eventServiceClient.searchEvents(basicParams);
                    appliedFilters.put("category", true);
                } catch (Exception e) {
                    System.err.println("Error with basic category search: " + e.getMessage());
                    failedFilters.add("category");
                    appliedFilters.put("category", false);
                }
            }

            // If we don't have results yet, try with no filters
            if (searchResponse == null || searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
                Map<String, String> minimalParams = new HashMap<>();
                minimalParams.put("page", "0");
                minimalParams.put("size", "5");
                minimalParams.put("sortBy", "date");
                minimalParams.put("sortDirection", "asc");

                try {
                    searchResponse = eventServiceClient.searchEvents(minimalParams);
                } catch (Exception e) {
                    System.err.println("Error with minimal search: " + e.getMessage());
                }
            }

            // Try to apply date filters if we have results
            if (dateFrom != null || dateTo != null) {
                Map<String, String> dateParams = new HashMap<>();
                if (category != null && !category.isEmpty() && appliedFilters.getOrDefault("category", false)) {
                    dateParams.put("category", category);
                }

                if (dateFrom != null) {
                    dateParams.put("dateFrom", dateFrom.toString());
                }

                if (dateTo != null) {
                    dateParams.put("dateTo", dateTo.toString());
                }

                dateParams.put("page", "0");
                dateParams.put("size", "5");
                dateParams.put("sortBy", "date");
                dateParams.put("sortDirection", "asc");

                try {
                    SearchEventResponse dateResponse = eventServiceClient.searchEvents(dateParams);
                    if (dateResponse != null && dateResponse.getResults() != null && !dateResponse.getResults().isEmpty()) {
                        searchResponse = dateResponse;
                        if (dateFrom != null) appliedFilters.put("dateFrom", true);
                        if (dateTo != null) appliedFilters.put("dateTo", true);
                    } else {
                        if (dateFrom != null) failedFilters.add("date range start");
                        if (dateTo != null) failedFilters.add("date range end");
                    }
                } catch (Exception e) {
                    System.err.println("Error with date filter search: " + e.getMessage());
                    if (dateFrom != null) failedFilters.add("date range start");
                    if (dateTo != null) failedFilters.add("date range end");
                }
            }

            // Try to apply price filters
            if (minPrice != null || maxPrice != null) {
                Map<String, String> priceParams = new HashMap<>();
                if (category != null && !category.isEmpty() && appliedFilters.getOrDefault("category", false)) {
                    priceParams.put("category", category);
                }

                if (minPrice != null) {
                    priceParams.put("minPrice", minPrice.toString());
                }

                if (maxPrice != null) {
                    priceParams.put("maxPrice", maxPrice.toString());
                }

                priceParams.put("page", "0");
                priceParams.put("size", "5");
                priceParams.put("sortBy", "date");
                priceParams.put("sortDirection", "asc");

                try {
                    SearchEventResponse priceResponse = eventServiceClient.searchEvents(priceParams);
                    if (priceResponse != null && priceResponse.getResults() != null && !priceResponse.getResults().isEmpty()) {
                        searchResponse = priceResponse;
                        if (minPrice != null) appliedFilters.put("minPrice", true);
                        if (maxPrice != null) appliedFilters.put("maxPrice", true);
                    } else {
                        if (minPrice != null) failedFilters.add("minimum price");
                        if (maxPrice != null) failedFilters.add("maximum price");
                    }
                } catch (Exception e) {
                    System.err.println("Error with price filter search: " + e.getMessage());
                    if (minPrice != null) failedFilters.add("minimum price");
                    if (maxPrice != null) failedFilters.add("maximum price");
                }
            }

            // Try location filters
            if (city != null && !city.isEmpty() || country != null && !country.isEmpty()) {
                Map<String, String> locationParams = new HashMap<>();
                if (category != null && !category.isEmpty() && appliedFilters.getOrDefault("category", false)) {
                    locationParams.put("category", category);
                }

                if (city != null && !city.isEmpty()) {
                    locationParams.put("city", city);
                }

                if (country != null && !country.isEmpty()) {
                    locationParams.put("country", country);
                }

                locationParams.put("page", "0");
                locationParams.put("size", "5");
                locationParams.put("sortBy", "date");
                locationParams.put("sortDirection", "asc");

                try {
                    SearchEventResponse locationResponse = eventServiceClient.searchEvents(locationParams);
                    if (locationResponse != null && locationResponse.getResults() != null && !locationResponse.getResults().isEmpty()) {
                        searchResponse = locationResponse;
                        if (city != null && !city.isEmpty()) appliedFilters.put("city", true);
                        if (country != null && !country.isEmpty()) appliedFilters.put("country", true);
                    } else {
                        if (city != null && !city.isEmpty()) failedFilters.add("city");
                        if (country != null && !country.isEmpty()) failedFilters.add("country");
                    }
                } catch (Exception e) {
                    System.err.println("Error with location filter search: " + e.getMessage());
                    if (city != null && !city.isEmpty()) failedFilters.add("city");
                    if (country != null && !country.isEmpty()) failedFilters.add("country");
                }
            }

            // Try venue filter
            if (venueName != null && !venueName.isEmpty()) {
                Map<String, String> venueParams = new HashMap<>();
                if (category != null && !category.isEmpty() && appliedFilters.getOrDefault("category", false)) {
                    venueParams.put("category", category);
                }

                venueParams.put("venueName", venueName);
                venueParams.put("page", "0");
                venueParams.put("size", "5");
                venueParams.put("sortBy", "date");
                venueParams.put("sortDirection", "asc");

                try {
                    SearchEventResponse venueResponse = eventServiceClient.searchEvents(venueParams);
                    if (venueResponse != null && venueResponse.getResults() != null && !venueResponse.getResults().isEmpty()) {
                        searchResponse = venueResponse;
                        appliedFilters.put("venueName", true);
                    } else {
                        failedFilters.add("venue");
                    }
                } catch (Exception e) {
                    System.err.println("Error with venue filter search: " + e.getMessage());
                    failedFilters.add("venue");
                }
            }

            // Build prompt with focus on event recommendations
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an event recommendation specialist for a ticketing platform. ");
            prompt.append("Focus on recommending events to users based on their interests and filters. ");

            // Add filter information to the prompt
            if (category != null && !category.isEmpty()) {
                prompt.append("The user is interested in ").append(category).append(" events. ");
            }

            if (minPrice != null || maxPrice != null) {
                prompt.append("Price range: ");
                if (minPrice != null) {
                    prompt.append("minimum $").append(minPrice);
                }
                if (minPrice != null && maxPrice != null) {
                    prompt.append(" to ");
                }
                if (maxPrice != null) {
                    prompt.append("maximum $").append(maxPrice);
                }
                prompt.append(". ");
            }

            if (dateFrom != null || dateTo != null) {
                prompt.append("Date range: ");
                if (dateFrom != null) {
                    prompt.append("from ").append(dateFrom);
                }
                if (dateFrom != null && dateTo != null) {
                    prompt.append(" to ");
                }
                if (dateTo != null) {
                    prompt.append("until ").append(dateTo);
                }
                prompt.append(". ");
            }

            if (city != null && !city.isEmpty()) {
                prompt.append("Location: ").append(city);
                if (country != null && !country.isEmpty()) {
                    prompt.append(", ").append(country);
                }
                prompt.append(". ");
            } else if (country != null && !country.isEmpty()) {
                prompt.append("Country: ").append(country).append(". ");
            }

            if (venueName != null && !venueName.isEmpty()) {
                prompt.append("Venue: ").append(venueName).append(". ");
            }

            if (numberOfPeople != null && numberOfPeople > 1) {
                prompt.append("Looking for events suitable for a group of ").append(numberOfPeople).append(" people. ");
            }

            // Add information about filters that couldn't be applied
            if (!failedFilters.isEmpty()) {
                prompt.append("\nNote: I couldn't filter by the following criteria due to system limitations: ");
                prompt.append(String.join(", ", failedFilters));
                prompt.append(". The recommendations below may not match all your requirements. ");
            }

            prompt.append("\nHere are some events that might interest the user:\n\n");

            // Add event details to the prompt
            if (searchResponse != null) {
                // Create an empty list as fallback
                List<SearchEventResponse.EventSummary> eventList = new ArrayList<>();

                // Try to extract events from the response
                try {
                    // Use reflection to access the results field
                    java.lang.reflect.Field resultsField = searchResponse.getClass().getDeclaredField("results");
                    resultsField.setAccessible(true);
                    Object resultsObj = resultsField.get(searchResponse);
                    if (resultsObj instanceof List) {
                        eventList = (List<SearchEventResponse.EventSummary>) resultsObj;
                    }
                } catch (Exception ex) {
                    // Ignore errors accessing results
                }

                if (eventList != null && !eventList.isEmpty()) {
                    for (int i = 0; i < eventList.size(); i++) {
                        prompt.append("- Event ").append(i+1).append(": ");

                        try {
                            // Just use simple string representation of the event
                            prompt.append(eventList.get(i).toString());
                        } catch (Exception ex2) {
                            // If any field access fails, just provide basic info
                            prompt.append("Details unavailable");
                        }

                        prompt.append("\n");
                    }
                } else {
                    prompt.append("No events found matching the criteria.\n");
                }
            } else {
                prompt.append("No events found matching the criteria.\n");
            }

            prompt.append("\nUser question: ").append(request.getQuery());

            // Get response from LLM
            ChatbotResponse response = callDeepSeekForEventRecommendations(prompt.toString(), request);

            // Save chat history
            saveChatHistory(request, response);

            return response;
        } catch (Exception e) {
            // Log error
            System.err.println("Error processing event recommendations: " + e.getMessage());
            e.printStackTrace();

            // Try to get some basic recommendations without filters
            try {
                Map<String, String> minimalParams = new HashMap<>();
                minimalParams.put("page", "0");
                minimalParams.put("size", "5");
                minimalParams.put("sortBy", "date");
                minimalParams.put("sortDirection", "asc");

                searchResponse = eventServiceClient.searchEvents(minimalParams);

                StringBuilder fallbackPrompt = new StringBuilder();
                fallbackPrompt.append("You are an event recommendation specialist for a ticketing platform. ");
                fallbackPrompt.append("The user asked for specific event recommendations, but our filtering system encountered an error. ");
                fallbackPrompt.append("Please apologize for not being able to apply their specific filters and offer these general recommendations instead. ");

                if (category != null && !category.isEmpty()) {
                    fallbackPrompt.append("They were looking for ").append(category).append(" events. ");
                }

                fallbackPrompt.append("\nHere are some general events that might interest the user:\n\n");

                if (searchResponse != null) {
                    // Create an empty list as fallback
                    List<SearchEventResponse.EventSummary> eventList = new ArrayList<>();

                    // Try to extract events from the response
                    if (searchResponse != null) {
                        try {
                            // Use reflection to access the results field
                            java.lang.reflect.Field resultsField = searchResponse.getClass().getDeclaredField("results");
                            resultsField.setAccessible(true);
                            Object resultsObj = resultsField.get(searchResponse);
                            if (resultsObj instanceof List) {
                                eventList = (List<SearchEventResponse.EventSummary>) resultsObj;
                            }
                        } catch (Exception ex) {
                            // Ignore errors accessing results
                        }
                    }

                    if (eventList != null && !eventList.isEmpty()) {
                        for (int i = 0; i < eventList.size(); i++) {
                            fallbackPrompt.append("- Event ").append(i+1).append(": ");

                            try {
                                // Just use simple string representation of the event
                                fallbackPrompt.append(eventList.get(i).toString());
                            } catch (Exception ex2) {
                                // If any field access fails, just provide basic info
                                fallbackPrompt.append("Details unavailable");
                            }

                            fallbackPrompt.append("\n");
                        }
                    } else {
                        fallbackPrompt.append("No events found in our system at the moment.\n");
                    }
                } else {
                    fallbackPrompt.append("No events found in our system at the moment.\n");
                }

                fallbackPrompt.append("\nUser question: ").append(request.getQuery());

                ChatbotResponse response = callDeepSeekForEventRecommendations(fallbackPrompt.toString(), request);
                saveChatHistory(request, response);
                return response;

            } catch (Exception fallbackError) {
                System.err.println("Error with fallback recommendations: " + fallbackError.getMessage());

                // Ultimate fallback response if everything fails
                ChatbotResponse response = new ChatbotResponse();
                response.setAnswer(
                        "I apologize, but I'm having trouble accessing our event database at the moment. " +
                        "Please try again later with simpler filters or contact customer service for assistance " +
                        "in finding events that match your specific requirements.");
                response.setConfidence(0.5);

                // Add some generic related FAQs
                List<ChatbotResponse.FAQ> relatedFaqs = new ArrayList<>();

                ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
                faq1.setQuestion("What events are trending this month?");
                faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
                relatedFaqs.add(faq1);

                ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
                faq2.setQuestion("How can I browse all upcoming events?");
                faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
                relatedFaqs.add(faq2);

                response.setRelatedFaq(relatedFaqs);

                return response;
            }
        }
    }

    private ChatbotResponse callDeepSeekForEventRecommendations(String prompt, ChatbotRequest request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are an event recommendation specialist helping users discover events they might enjoy."));
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
            response.setConfidence(0.9); // High confidence for recommendations

            // Recommendation-specific related FAQs
            List<ChatbotResponse.FAQ> relatedFaqs = new ArrayList<>();

            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("How do I book tickets for these events");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("Are there any upcoming concerts");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq2);

            ChatbotResponse.FAQ faq3 = new ChatbotResponse.FAQ();
            faq3.setQuestion("What are the most popular events this month");
            faq3.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            relatedFaqs.add(faq3);

            // Randomly select 2 FAQs to show
            Collections.shuffle(relatedFaqs);
            relatedFaqs = relatedFaqs.subList(0, Math.min(2, relatedFaqs.size()));

            response.setRelatedFaq(relatedFaqs);

            return response;
        } catch (Exception e) {
            // Log error
            System.err.println("Error calling DeepSeek API for event recommendations: " + e.getMessage());
            e.printStackTrace();

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(
                    "Sorry, I couldn't generate event recommendations at the moment. Please try again later or contact customer service for assistance.");
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