package com.luxshop.shop.controller;

import com.luxshop.shop.dto.ProductResponse;
import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.service.RagService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Semantic (meaning-based) product search backed by the vector store — e.g.
 * "a gift for a diver" surfaces the dive watch. Public and postgres-only, since
 * it depends on the RAG stack.
 */
@RestController
@RequestMapping("/shop/search")
@Profile("postgres")
public class SemanticSearchController {

    private final RagService ragService;
    private final ProductRepository productRepository;

    public SemanticSearchController(RagService ragService, ProductRepository productRepository) {
        this.ragService = ragService;
        this.productRepository = productRepository;
    }

    @GetMapping("/semantic")
    public List<ProductResponse> semantic(@RequestParam String q,
                                          @RequestParam(defaultValue = "5") int topK) {
        // Retrieve the closest product ids, then load them in relevance order.
        return ragService.search(q, Math.max(1, Math.min(topK, 20))).stream()
                .map(id -> productRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(ProductResponse::from)
                .toList();
    }
}
