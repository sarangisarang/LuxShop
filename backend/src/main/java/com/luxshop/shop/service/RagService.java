package com.luxshop.shop.service;

import com.luxshop.shop.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RAG retrieval over the product catalog: turns products into embedded documents
 * in the pgvector store and answers semantic queries against them. Only active
 * under the postgres profile (the vector store needs Postgres + the API key),
 * so the H2 test profile never instantiates it.
 */
@Service
@Profile("postgres")
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** (Re)index a batch of products — one embedded document each. */
    public void reindex(List<Product> products) {
        List<Document> docs = products.stream().map(this::toDocument).toList();
        if (!docs.isEmpty()) {
            vectorStore.add(docs);
        }
        log.info("Indexed {} products into the vector store", docs.size());
    }

    /** Product ids of the semantically closest matches, best first. */
    public List<String> search(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.query(query).withTopK(topK))
                .stream()
                .map(d -> String.valueOf(d.getMetadata().get("productId")))
                .toList();
    }

    private Document toDocument(Product product) {
        String category = product.getCategory() != null ? product.getCategory().getName() : "Uncategorized";
        // A rich, natural-language representation embeds better than raw fields.
        String content = String.format(
                "Product: %s. Description: %s. Category: %s. Price: %s GEL.",
                product.getProductName(), product.getProductDesc(), category, product.getPrice());
        return new Document(content, Map.of(
                "productId", product.getId(),
                "category", category,
                "price", product.getPrice() != null ? product.getPrice().doubleValue() : 0.0));
    }
}
