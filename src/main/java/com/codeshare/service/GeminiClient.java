package com.codeshare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.7-flash}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateExplanation(String code, String language) throws Exception {
        String resolvedKey = apiKey;
        if (resolvedKey == null || resolvedKey.trim().isEmpty()) {
            resolvedKey = System.getenv("GEMINI_API_KEY");
        }

        if (resolvedKey == null || resolvedKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable to enable AI code explanations.");
        }

        String prompt = "You are an expert developer assistant. Explain the following " + language + " code snippet.\n\n" +
                "Provide:\n" +
                "1. **Overview**: A high-level description of what the code does.\n" +
                "2. **Breakdown**: A logical step-by-step explanation of the key functions, loops, or logic.\n" +
                "3. **Key Takeaways**: Best practices, potential bugs, or performance notes.\n\n" +
                "Format your explanation in clean, professional markdown. Here is the code:\n\n" +
                code;

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentObject = contentsArray.addObject();
        ArrayNode partsArray = contentObject.putArray("parts");
        ObjectNode partObject = partsArray.addObject();
        partObject.put("text", prompt);

        // Configure thinkingConfig for gemini-3.7-flash with low thinking level for fast response
        ObjectNode generationConfig = rootNode.putObject("generationConfig");
        ObjectNode thinkingConfig = generationConfig.putObject("thinkingConfig");
        thinkingConfig.put("thinkingLevel", "low");

        String jsonRequestBody = objectMapper.writeValueAsString(rootNode);
        String resolvedModel = (model != null && !model.trim().isEmpty()) ? model.trim() : "gemini-3.7-flash";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + resolvedModel + ":generateContent?key=" + resolvedKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String details = "";
            try {
                JsonNode errorRoot = objectMapper.readTree(response.body());
                details = errorRoot.path("error").path("message").asText();
            } catch (Exception ignored) {
            }
            if (details != null && !details.trim().isEmpty()) {
                throw new RuntimeException("Gemini API error (" + response.statusCode() + "): " + details);
            }
            throw new RuntimeException("Failed to call Gemini API. HTTP status code: " + response.statusCode());
        }

        JsonNode responseRoot = objectMapper.readTree(response.body());
        String explanation = responseRoot.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

        if (explanation == null || explanation.trim().isEmpty()) {
            throw new RuntimeException("Received empty explanation from Gemini API.");
        }

        return explanation;
    }
}
