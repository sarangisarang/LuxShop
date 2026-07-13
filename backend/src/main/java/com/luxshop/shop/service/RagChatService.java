package com.luxshop.shop.service;

import com.luxshop.shop.config.GeminiChatClient;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.dto.AssistantResponse;
import com.luxshop.shop.dto.ProductResponse;
import com.luxshop.shop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The RAG shopping assistant: retrieve the products most relevant to the
 * question (vector search), then ask Gemini to answer grounded in ONLY those
 * products. Returns the reply plus the products, so the UI can show cards.
 * Postgres profile only.
 */
@Service
@Profile("postgres")
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);
    private static final int RETRIEVE = 4;

    private final RagService ragService;
    private final GeminiChatClient chatClient;
    private final ProductRepository productRepository;

    public RagChatService(RagService ragService, GeminiChatClient chatClient,
                          ProductRepository productRepository) {
        this.ragService = ragService;
        this.chatClient = chatClient;
        this.productRepository = productRepository;
    }

    public AssistantResponse answer(String message) {
        // Retrieve: the products semantically closest to the question.
        List<Product> products = ragService.search(message, RETRIEVE).stream()
                .map(id -> productRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        String catalog = products.stream()
                .map(p -> String.format("- %s (%s, %s GEL): %s",
                        p.getProductName(),
                        p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                        p.getPrice(), p.getProductDesc()))
                .collect(Collectors.joining("\n"));

        // Reply in whatever language the shopper actually wrote in, so the answer
        // always matches the question regardless of the UI language.
        String system = "You are LuxShop's friendly shopping assistant. Recommend ONLY from the "
                + "catalog products provided. Be concise (2-3 sentences), warm, and explain why the "
                + "pick fits. If nothing fits, say so honestly. Always reply in the exact same "
                + "language the customer used in their question.";
        String user = "Catalog:\n" + catalog + "\n\nCustomer question: " + message;

        // If Gemini is briefly overloaded (503/429) or otherwise unavailable, degrade
        // gracefully: the vector search already found relevant products, so still return
        // them with a friendly note instead of failing the whole request.
        String answer;
        try {
            answer = chatClient.chat(system, user);
        } catch (Exception e) {
            log.warn("Assistant chat unavailable ({}); returning products without an AI reply.", e.getMessage());
            answer = products.isEmpty()
                    ? "Our AI assistant is briefly unavailable — please try again in a moment."
                    : "Our AI assistant is briefly busy, but here are some products from our catalog that match your request.";
        }
        if (answer.isBlank()) {
            answer = "Sorry, I couldn't find a good match in our catalog right now.";
        }
        return new AssistantResponse(answer, products.stream().map(ProductResponse::from).toList());
    }
}
