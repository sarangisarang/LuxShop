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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    // Retrieve a few extra candidates; the model then keeps only the ones that
    // genuinely fit, which also compensates for weaker cross-lingual matches.
    private static final int RETRIEVE = 6;
    private static final String IDS_MARKER = "RECOMMENDED_IDS:";

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
        // Retrieve candidates; the model decides which of them actually fit.
        List<Product> candidates = ragService.search(message, RETRIEVE).stream()
                .map(id -> productRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        String catalog = candidates.stream()
                .map(p -> String.format("- [id=%s] %s (%s, %s GEL): %s",
                        p.getId(), p.getProductName(),
                        p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                        p.getPrice(), p.getProductDesc()))
                .collect(Collectors.joining("\n"));

        // Reply in whatever language the shopper wrote in (so the answer matches the
        // question regardless of UI language), and have the model tell us which
        // products it actually recommends so we never show unrelated cards.
        String system = "You are LuxShop's friendly shopping assistant. Recommend ONLY from the "
                + "catalog products provided, and ONLY the ones that genuinely fit the request — "
                + "never pad the answer with unrelated items. Be concise (2-3 sentences), warm, and "
                + "explain why the pick fits. If nothing fits, say so honestly. Always reply in the "
                + "exact same language the customer used in their question. Then, on a separate final "
                + "line, output exactly '" + IDS_MARKER + "' followed by the id numbers (from each "
                + "[id=...] tag) of the products you recommend, comma-separated and most relevant "
                + "first. If nothing fits, write '" + IDS_MARKER + " none'.";
        String user = "Catalog:\n" + catalog + "\n\nCustomer question: " + message;

        // If Gemini is briefly overloaded (503/429) or otherwise unavailable, degrade
        // gracefully: the vector search already found relevant products, so still return
        // them with a friendly note instead of failing the whole request.
        String raw;
        try {
            raw = chatClient.chat(system, user);
        } catch (Exception e) {
            log.warn("Assistant chat unavailable ({}); returning products without an AI reply.", e.getMessage());
            String note = candidates.isEmpty()
                    ? "Our AI assistant is briefly unavailable — please try again in a moment."
                    : "Our AI assistant is briefly busy, but here are some products from our catalog that match your request.";
            return new AssistantResponse(note, candidates.stream().map(ProductResponse::from).toList());
        }

        // Split the recommended ids off the reply and show only those cards.
        List<Product> recommended = candidates;
        String answer = raw.strip();
        int idx = raw.lastIndexOf(IDS_MARKER);
        if (idx >= 0) {
            answer = raw.substring(0, idx).strip();
            Map<String, Product> byId = candidates.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a, LinkedHashMap::new));
            recommended = Arrays.stream(raw.substring(idx + IDS_MARKER.length()).split("[,\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        if (answer.isBlank()) {
            answer = "Sorry, I couldn't find a good match in our catalog right now.";
        }
        return new AssistantResponse(answer, recommended.stream().map(ProductResponse::from).toList());
    }
}
