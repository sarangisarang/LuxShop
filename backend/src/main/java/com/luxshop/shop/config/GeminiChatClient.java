package com.luxshop.shop.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

    private static final Logger log = LoggerFactory.getLogger(GeminiChatClient.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 500L;

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

    /**
     * One-shot completion; returns the assistant text (never null). Retries a few
     * times with backoff on transient upstream errors (503 overload, 429 rate
     * limit), which Gemini emits under load; other errors propagate immediately.
     */
    public String chat(String systemPrompt, String userPrompt) {
        RestClientResponseException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return callOnce(systemPrompt, userPrompt);
            } catch (RestClientResponseException e) {
                int code = e.getStatusCode().value();
                if ((code != 503 && code != 429) || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                last = e;
                log.warn("Gemini chat transient {} (attempt {}/{}); retrying.", code, attempt, MAX_ATTEMPTS);
                sleep(BASE_BACKOFF_MS * attempt);
            }
        }
        throw last; // unreachable: the loop either returns or throws above
    }

    private String callOnce(String systemPrompt, String userPrompt) {
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

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
