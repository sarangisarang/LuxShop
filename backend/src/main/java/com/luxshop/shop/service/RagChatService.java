package com.luxshop.shop.service;

import com.luxshop.shop.config.GeminiChatClient;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.dto.AssistantResponse;
import com.luxshop.shop.dto.ProductResponse;
import com.luxshop.shop.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
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

        // Reply in the shopper's UI language (resolved from Accept-Language).
        String language = LocaleContextHolder.getLocale().getDisplayLanguage(Locale.ENGLISH);
        String system = "You are LuxShop's friendly shopping assistant. Recommend ONLY from the "
                + "catalog products provided. Be concise (2-3 sentences), warm, and explain why the "
                + "pick fits. If nothing fits, say so honestly. Always reply in " + language + ".";
        String user = "Catalog:\n" + catalog + "\n\nCustomer question: " + message;

        String answer = chatClient.chat(system, user);
        if (answer.isBlank()) {
            answer = "Sorry, I couldn't find a good match in our catalog right now.";
        }
        return new AssistantResponse(answer, products.stream().map(ProductResponse::from).toList());
    }
}
