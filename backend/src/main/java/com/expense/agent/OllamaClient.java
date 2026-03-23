package com.expense.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${huggingface.api-key:}")
    private String hfApiKey;

    @Value("${huggingface.model:mistralai/Mistral-7B-Instruct-v0.1}")
    private String hfModel;

    @Value("${huggingface.enabled:false}")
    private boolean hfEnabled;

    @Value("${groq.api-key:}")
    private String groqApiKey;

    @Value("${groq.model:llama3-8b-8192}")
    private String groqModel;

    @Value("${groq.enabled:false}")
    private boolean groqEnabled;

    private final ObjectMapper objectMapper;

    // Priority: Groq → HuggingFace → Ollama → null (triggers fallback in agents)
    public String prompt(String systemPrompt, String userMessage) {
        if (groqEnabled && !groqApiKey.isBlank() && !groqApiKey.equals("your_groq_api_key_here")) {
            String result = callGroq(systemPrompt, userMessage);
            if (result != null) return result;
        }
        if (hfEnabled && !hfApiKey.isBlank()) {
            String result = callHuggingFace(systemPrompt, userMessage);
            if (result != null) return result;
        }
        return callOllama(systemPrompt, userMessage);
    }

    private String callGroq(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> body = Map.of(
                "model", groqModel,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 500,
                "temperature", 0.3
            );

            String response = WebClient.create("https://api.groq.com")
                .post().uri("/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .bodyValue(body)
                .retrieve().bodyToMono(String.class).block();

            JsonNode node = objectMapper.readTree(response);
            String content = node.get("choices").get(0).get("message").get("content").asText().trim();
            log.info("Groq AI responded successfully");
            return content;
        } catch (Exception e) {
            log.warn("Groq call failed: {}", e.getMessage());
            return null;
        }
    }

    private String callHuggingFace(String systemPrompt, String userMessage) {
        try {
            String input = systemPrompt + "\n\nUser: " + userMessage + "\nAssistant:";
            String response = WebClient.create("https://router.huggingface.co")
                .post().uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + hfApiKey)
                .bodyValue(Map.of(
                    "model", hfModel,
                    "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                    ),
                    "max_tokens", 500
                ))
                .retrieve().bodyToMono(String.class).block();

            JsonNode node = objectMapper.readTree(response);
            return node.get("choices").get(0).get("message").get("content").asText().trim();
        } catch (Exception e) {
            log.warn("HuggingFace call failed: {}", e.getMessage());
            return null;
        }
    }

    private String callOllama(String systemPrompt, String userMessage) {
        try {
            String fullPrompt = systemPrompt + "\n\nUser: " + userMessage + "\nAssistant:";
            String response = WebClient.create(ollamaUrl)
                .post().uri("/api/generate")
                .bodyValue(Map.of("model", ollamaModel, "prompt", fullPrompt, "stream", false))
                .retrieve().bodyToMono(String.class).block();
            return objectMapper.readTree(response).get("response").asText().trim();
        } catch (Exception e) {
            log.warn("Ollama unavailable: {}", e.getMessage());
            return null;
        }
    }
}
