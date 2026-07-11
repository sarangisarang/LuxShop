package com.luxshop.shop.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Minimal chat client for Gemini's OpenAI-compatible chat endpoint. A hand-rolled
 * client (rather than Spring AI's ChatModel) keeps control of the path
 * (/chat/completions) and a generous token budget — gemini-flash-latest is a
 * "thinking" model that spends tokens reasoning before it emits any answer.
 * Postgres profile only.
 */
@Component
@Profile("postgres")
public class GeminiChatClient {

    private final RestClient client;
    private final String model;
    private final int maxTokens;

    public GeminiChatClient(
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model}") String model,
            @Value("${luxshop.assistant.max-tokens:2000}") int maxTokens) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.maxTokens = maxTokens;
    }

    /** One-shot completion; returns the assistant text (never null). */
    public String chat(String systemPrompt, String userPrompt) {
        ChatResponse resp = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "model", model,
                        "max_tokens", maxTokens,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt))))
                .retrieve()
                .body(ChatResponse.class);

        if (resp == null || resp.choices() == null || resp.choices().isEmpty()) {
            return "";
        }
        String content = resp.choices().get(0).message().content();
        return content != null ? content.strip() : "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Choice(Message message) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Message(String content) {
        }
    }
}
