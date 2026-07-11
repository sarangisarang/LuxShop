package com.luxshop.shop.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minimal EmbeddingModel for Gemini's OpenAI-compatible embeddings endpoint.
 *
 * Spring AI's own OpenAiEmbeddingModel rejects Gemini's response because it omits
 * the `usage` object ("OpenAI Usage must not be null"). This client parses only
 * the fields Gemini returns, so it works with just a Gemini API key.
 */
public class GeminiEmbeddingModel implements EmbeddingModel {

    private final RestClient client;
    private final String model;
    private final int dimensions;

    public GeminiEmbeddingModel(String baseUrl, String apiKey, String model, int dimensions) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> results = new ArrayList<>();
        List<String> inputs = request.getInstructions();
        for (int i = 0; i < inputs.size(); i++) {
            GeminiResponse resp = client.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "input", inputs.get(i), "dimensions", dimensions))
                    .retrieve()
                    .body(GeminiResponse.class);
            results.add(new Embedding(resp.data().get(0).embedding(), i));
        }
        return new EmbeddingResponse(results);
    }

    @Override
    public float[] embed(Document document) {
        return call(new EmbeddingRequest(List.of(document.getContent()), null))
                .getResults().get(0).getOutput();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Item> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Item(float[] embedding) {
        }
    }
}
