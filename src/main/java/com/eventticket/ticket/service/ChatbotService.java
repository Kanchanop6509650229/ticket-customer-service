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
            System.err.println("Error calling DeepSeek API: " + e.getMessage());

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();

            // Create a more helpful fallback response
            String fallbackAnswer = "I'm sorry, but I'm having trouble connecting to my knowledge base at the moment. " +
                    "This could be due to network issues or high demand. " +
                    "In the meantime, you can:\n\n" +
                    "• Try asking a different question\n" +
                    "• Check our FAQ section on the website\n" +
                    "• Contact customer service at support@eventticket.com\n" +
                    "• Try again in a few minutes";

            response.setAnswer(fallbackAnswer);
            response.setConfidence(0.5);

            // Add some helpful FAQs
            List<ChatbotResponse.FAQ> fallbackFaqs = new ArrayList<>();
            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("View frequently asked questions");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("Contact customer support");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq2);

            response.setRelatedFaq(fallbackFaqs);

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
            System.err.println("Error calling DeepSeek API for event info: " + e.getMessage());

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();

            // Create a more helpful fallback response
            String fallbackAnswer = "I'm sorry, but I'm having trouble retrieving event information at the moment. " +
                    "This could be due to network issues or high demand. " +
                    "In the meantime, you can:\n\n" +
                    "• Try asking a different question\n" +
                    "• Browse events on our main page\n" +
                    "• Contact customer service at support@eventticket.com\n" +
                    "• Try again in a few minutes";

            response.setAnswer(fallbackAnswer);
            response.setConfidence(0.5);

            // Add some helpful FAQs
            List<ChatbotResponse.FAQ> fallbackFaqs = new ArrayList<>();
            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("Browse all events");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("Contact customer support");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq2);

            response.setRelatedFaq(fallbackFaqs);

            return response;
        }
    }

    /**
     * Process event recommendations based on user query
     * Uses AI to determine search parameters from the user's natural language query
     */
    public ChatbotResponse processEventRecommendations(ChatbotRequest request) {
        try {
            // First, use AI to analyze the query and determine search parameters
            Map<String, String> queryParams = determineSearchParameters(request.getQuery());

            // Set default parameters for recommendations if not already set
            if (!queryParams.containsKey("page")) {
                queryParams.put("page", "0");
            }
            if (!queryParams.containsKey("size")) {
                queryParams.put("size", "5");
            }
            if (!queryParams.containsKey("sortBy")) {
                queryParams.put("sortBy", "date");
            }
            if (!queryParams.containsKey("sortDirection")) {
                queryParams.put("sortDirection", "asc");
            }

            // Call the event service to search for events
            SearchEventResponse searchResponse = eventServiceClient.searchEvents(queryParams);

            // Build prompt with focus on event recommendations
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an event recommendation specialist for a ticketing platform. ");
            prompt.append("Focus on recommending events to users based on their interests. ");

            // Add context about what the user is looking for
            if (queryParams.containsKey("category")) {
                prompt.append("The user is interested in ").append(queryParams.get("category")).append(" events. ");
            }
            if (queryParams.containsKey("keyword")) {
                prompt.append("The user is looking for events related to '").append(queryParams.get("keyword")).append("'. ");
            }
            if (queryParams.containsKey("dateFrom") || queryParams.containsKey("dateTo")) {
                prompt.append("The user is looking for events ");
                if (queryParams.containsKey("dateFrom")) {
                    prompt.append("from ").append(queryParams.get("dateFrom")).append(" ");
                }
                if (queryParams.containsKey("dateTo")) {
                    prompt.append("until ").append(queryParams.get("dateTo")).append(" ");
                }
                prompt.append(". ");
            }

            prompt.append("Here are some events that might interest the user:\n\n");

            // Add event details to the prompt
            if (searchResponse != null && searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                for (SearchEventResponse.EventSummary event : searchResponse.getResults()) {
                    prompt.append("- ").append(event.getName())
                          .append(" (").append(event.getDate()).append(")")
                          .append(" at ").append(event.getVenue());

                    if (event.getTicketPrice() != null) {
                        prompt.append(", tickets from $").append(event.getTicketPrice().getMin())
                              .append(" to $").append(event.getTicketPrice().getMax());
                    }

                    prompt.append("\n");
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

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();
            response.setAnswer(
                    "Sorry, I couldn't find any event recommendations at the moment. Please try again later or contact customer service for assistance.");
            response.setConfidence(0.5);
            return response;
        }
    }

    /**
     * For backward compatibility
     */
    public ChatbotResponse processEventRecommendations(ChatbotRequest request, String category) {
        // If category is explicitly provided, use it in the search parameters
        if (category != null && !category.isEmpty()) {
            try {
                // Create a map of query parameters for the search
                Map<String, String> queryParams = determineSearchParameters(request.getQuery());

                // Override with the explicitly provided category
                queryParams.put("category", category);

                // Set default parameters for recommendations if not already set
                if (!queryParams.containsKey("page")) {
                    queryParams.put("page", "0");
                }
                if (!queryParams.containsKey("size")) {
                    queryParams.put("size", "5");
                }
                if (!queryParams.containsKey("sortBy")) {
                    queryParams.put("sortBy", "date");
                }
                if (!queryParams.containsKey("sortDirection")) {
                    queryParams.put("sortDirection", "asc");
                }

                // Call the event service to search for events
                SearchEventResponse searchResponse = eventServiceClient.searchEvents(queryParams);

                // Build prompt with focus on event recommendations
                StringBuilder prompt = new StringBuilder();
                prompt.append("You are an event recommendation specialist for a ticketing platform. ");
                prompt.append("Focus on recommending events to users based on their interests. ");
                prompt.append("The user is interested in ").append(category).append(" events. ");

                // Add context about other search parameters
                if (queryParams.containsKey("keyword")) {
                    prompt.append("The user is looking for events related to '").append(queryParams.get("keyword")).append("'. ");
                }
                if (queryParams.containsKey("dateFrom") || queryParams.containsKey("dateTo")) {
                    prompt.append("The user is looking for events ");
                    if (queryParams.containsKey("dateFrom")) {
                        prompt.append("from ").append(queryParams.get("dateFrom")).append(" ");
                    }
                    if (queryParams.containsKey("dateTo")) {
                        prompt.append("until ").append(queryParams.get("dateTo")).append(" ");
                    }
                    prompt.append(". ");
                }

                prompt.append("Here are some events that might interest the user:\n\n");

                // Add event details to the prompt
                if (searchResponse != null && searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                    for (SearchEventResponse.EventSummary event : searchResponse.getResults()) {
                        prompt.append("- ").append(event.getName())
                              .append(" (").append(event.getDate()).append(")")
                              .append(" at ").append(event.getVenue());

                        if (event.getTicketPrice() != null) {
                            prompt.append(", tickets from $").append(event.getTicketPrice().getMin())
                                  .append(" to $").append(event.getTicketPrice().getMax());
                        }

                        prompt.append("\n");
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
                System.err.println("Error processing event recommendations with category: " + e.getMessage());
                e.printStackTrace();

                // Fallback response
                ChatbotResponse response = new ChatbotResponse();
                response.setAnswer(
                        "Sorry, I couldn't find any event recommendations at the moment. Please try again later or contact customer service for assistance.");
                response.setConfidence(0.5);
                return response;
            }
        } else {
            // If no category provided, use the Regular method
            return processEventRecommendations(request);
        }
    }

    /**
     * Use AI to determine search parameters from the user's query
     * This is a more flexible approach than keyword matching
     * Includes validation to prevent invalid parameters
     */
    private Map<String, String> determineSearchParameters(String query) {
        Map<String, String> extractedParams = new HashMap<>();

        try {
            // Create a prompt for the AI to extract search parameters
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an AI assistant that helps extract search parameters from user queries about events. ");
            prompt.append("Based on the following user query, determine the appropriate search parameters. ");
            prompt.append("Return ONLY a JSON object with the following possible fields (include only if mentioned or implied in the query):\n");
            prompt.append("- category: The type of event (e.g., Concert, Sports, Theater, Conference, Festival, Exhibition)\n");
            prompt.append("- keyword: Any specific search terms\n");
            prompt.append("- dateFrom: Start date in YYYY-MM-DD format\n");
            prompt.append("- dateTo: End date in YYYY-MM-DD format\n");
            prompt.append("- minPrice: Minimum price as a number\n");
            prompt.append("- maxPrice: Maximum price as a number\n");
            prompt.append("- city: City name\n");
            prompt.append("- country: Country name\n\n");
            prompt.append("User query: \"").append(query).append("\"\n\n");
            prompt.append("JSON response (include only relevant fields, and ONLY use the exact field names listed above):");

            // Call the AI to extract parameters
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                "You are a parameter extraction assistant that returns only valid JSON. " +
                "Only extract parameters that are explicitly mentioned or strongly implied in the query. " +
                "Do not hallucinate or invent parameters that aren't in the query. " +
                "Use only the exact field names specified in the instructions."));
            messages.add(Map.of("role", "user", "content", prompt.toString()));
            requestBody.put("messages", messages);

            requestBody.put("temperature", 0.1); // Low temperature for more deterministic output
            requestBody.put("max_tokens", 200);

            String jsonResponse = deepSeekWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(jsonResponse);
            String content = responseJson.path("choices").path(0).path("message").path("content").asText();

            // Extract the JSON part from the response
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            // Parse the JSON
            JsonNode paramsJson = objectMapper.readTree(content);

            // Convert to map of strings
            Iterator<Map.Entry<String, JsonNode>> fields = paramsJson.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (!field.getValue().isNull() && field.getValue().asText().length() > 0) {
                    extractedParams.put(field.getKey(), field.getValue().asText());
                }
            }

            System.out.println("Raw extracted search parameters: " + extractedParams);

        } catch (Exception e) {
            System.err.println("Error extracting search parameters: " + e.getMessage());
            // If there's an error, we'll return an empty map and let the calling method handle defaults
        }

        // Validate and sanitize the extracted parameters
        return validateAndSanitizeParameters(extractedParams);
    }

    /**
     * Validate and sanitize parameters to ensure they are supported by the API
     * This prevents the AI from generating parameters that don't exist or have invalid values
     */
    private Map<String, String> validateAndSanitizeParameters(Map<String, String> extractedParams) {
        Map<String, String> validatedParams = new HashMap<>();

        // Define the whitelist of allowed parameters
        Set<String> allowedParams = Set.of(
            "category", "keyword", "dateFrom", "dateTo",
            "minPrice", "maxPrice", "city", "country",
            "page", "size", "sortBy", "sortDirection"
        );

        // Define valid categories
        Set<String> validCategories = Set.of(
            "Concert", "Sports", "Theater", "Conference", "Festival", "Exhibition"
        );

        // Process each parameter
        for (Map.Entry<String, String> entry : extractedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Skip parameters not in the whitelist
            if (!allowedParams.contains(key)) {
                System.out.println("Skipping unsupported parameter: " + key);
                continue;
            }

            // Validate and sanitize specific parameters
            switch (key) {
                case "category":
                    // Ensure category is valid
                    if (validCategories.contains(value)) {
                        validatedParams.put(key, value);
                    } else {
                        System.out.println("Invalid category value: " + value);
                    }
                    break;

                case "dateFrom":
                case "dateTo":
                    // Validate date format (YYYY-MM-DD)
                    if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        validatedParams.put(key, value);
                    } else {
                        System.out.println("Invalid date format for " + key + ": " + value);
                    }
                    break;

                case "minPrice":
                case "maxPrice":
                    // Validate price is a number
                    try {
                        Double.parseDouble(value);
                        validatedParams.put(key, value);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid price format for " + key + ": " + value);
                    }
                    break;

                default:
                    // For other parameters, just pass them through
                    validatedParams.put(key, value);
                    break;
            }
        }

        System.out.println("Validated search parameters: " + validatedParams);
        return validatedParams;
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

            // Fallback response
            ChatbotResponse response = new ChatbotResponse();

            // Create a more helpful fallback response
            String fallbackAnswer = "I'm sorry, but I'm having trouble generating event recommendations at the moment. " +
                    "This could be due to network issues or high demand. " +
                    "In the meantime, you can:\n\n" +
                    "• Browse our featured events on the home page\n" +
                    "• Search for events by category or date\n" +
                    "• Contact customer service at support@eventticket.com\n" +
                    "• Try again in a few minutes";

            response.setAnswer(fallbackAnswer);
            response.setConfidence(0.5);

            // Add some helpful FAQs
            List<ChatbotResponse.FAQ> fallbackFaqs = new ArrayList<>();
            ChatbotResponse.FAQ faq1 = new ChatbotResponse.FAQ();
            faq1.setQuestion("View popular events");
            faq1.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq1);

            ChatbotResponse.FAQ faq2 = new ChatbotResponse.FAQ();
            faq2.setQuestion("Search events by category");
            faq2.setId("faq" + ThreadLocalRandom.current().nextInt(1000));
            fallbackFaqs.add(faq2);

            response.setRelatedFaq(fallbackFaqs);

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