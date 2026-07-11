package com.luxshop.shop.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Wires the embedding client for Gemini's OpenAI-compatible endpoint. Overrides
 * Spring AI's default OpenAI embedding model (which rejects Gemini's usage-less
 * response) with a minimal Gemini client. Postgres profile only (RAG stack).
 */
@Configuration
@Profile("postgres")
public class RagAiConfig {

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.embedding.options.model}") String model,
            // gemini-embedding-001 defaults to 3072 dims but honours a requested
            // size; keep it in step with the pgvector column width.
            @Value("${spring.ai.vectorstore.pgvector.dimensions:768}") int dimensions) {
        return new GeminiEmbeddingModel(baseUrl, apiKey, model, dimensions);
    }
}
