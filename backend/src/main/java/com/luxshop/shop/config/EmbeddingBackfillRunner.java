package com.luxshop.shop.config;

import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * On startup (postgres profile), embed the catalog into the vector store once —
 * only when it is still empty, so restarts don't re-embed. Failures (missing /
 * invalid API key, no network) are logged and swallowed so the app still boots;
 * semantic search simply stays empty until a valid key is provided.
 */
@Component
@Profile("postgres")
public class EmbeddingBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingBackfillRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final ProductRepository productRepository;
    private final RagService ragService;

    public EmbeddingBackfillRunner(JdbcTemplate jdbcTemplate,
                                   ProductRepository productRepository,
                                   RagService ragService) {
        this.jdbcTemplate = jdbcTemplate;
        this.productRepository = productRepository;
        this.ragService = ragService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM vector_store", Integer.class);
            if (count != null && count > 0) {
                log.info("Vector store already has {} documents — skipping embedding backfill", count);
                return;
            }
            ragService.reindex(productRepository.findAll());
        } catch (Exception e) {
            log.warn("RAG embedding backfill skipped: {}. Set a valid GEMINI_API_KEY (with network access) "
                    + "to enable semantic search.", e.getMessage());
        }
    }
}
