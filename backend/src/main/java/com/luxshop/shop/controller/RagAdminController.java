package com.luxshop.shop.controller;

import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.service.RagService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin RAG maintenance: rebuild the product embeddings after catalog changes.
 * Not listed in the security permit-list, so it requires a Bearer token like the
 * other admin endpoints. Postgres profile only.
 */
@RestController
@RequestMapping("/shop/rag")
@Profile("postgres")
public class RagAdminController {

    private final RagService ragService;
    private final ProductRepository productRepository;

    public RagAdminController(RagService ragService, ProductRepository productRepository) {
        this.ragService = ragService;
        this.productRepository = productRepository;
    }

    @PostMapping("/reindex")
    public Map<String, Integer> reindex() {
        int indexed = ragService.reindexAll(productRepository.findAll());
        return Map.of("indexed", indexed);
    }
}
