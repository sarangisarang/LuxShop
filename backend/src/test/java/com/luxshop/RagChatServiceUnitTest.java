package com.luxshop;

import com.luxshop.shop.config.GeminiChatClient;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.dto.AssistantResponse;
import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.service.RagChatService;
import com.luxshop.shop.service.RagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * DB-free unit tests for the RAG assistant's answer assembly — the two pieces
 * of behaviour added recently that have no integration coverage (the RAG stack
 * is postgres-profile-only, so it never loads under the H2 test context):
 *   - only the products the model recommends via RECOMMENDED_IDS become cards (#88), and
 *   - a chat-client failure degrades gracefully to the retrieved products (#85).
 * Collaborators are mocked, so nothing here touches pgvector, Gemini or the DB.
 */
@ExtendWith(MockitoExtension.class)
class RagChatServiceUnitTest {

    @Mock
    private RagService ragService;
    @Mock
    private GeminiChatClient chatClient;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RagChatService ragChatService;

    private Product product(String id, String name, String category, String price) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setProductDesc(name + " description");
        p.setPrice(new BigDecimal(price));
        p.setStock(5);
        return p;
    }

    /** Retrieve three candidates (two watches + sunglasses) for every query. */
    private void retrieveWatchesAndSunglasses() {
        when(ragService.search(any(), anyInt())).thenReturn(List.of("1", "2", "12"));
        when(productRepository.findById("1")).thenReturn(Optional.of(product("1", "Rolex Submariner", "Watches", "38500")));
        when(productRepository.findById("2")).thenReturn(Optional.of(product("2", "Omega Speedmaster", "Watches", "21900")));
        when(productRepository.findById("12")).thenReturn(Optional.of(product("12", "Ray-Ban Aviator", "Accessories", "520")));
    }

    private List<String> cardIds(AssistantResponse res) {
        return res.products().stream().map(p -> p.id()).toList();
    }

    // --- #88: RECOMMENDED_IDS decides which candidates become cards ----------

    @Test
    void showsOnlyRecommendedProductsAndStripsTheMarker() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any()))
                .thenReturn("Here are two great watches for you.\nRECOMMENDED_IDS: 1, 2");

        AssistantResponse res = ragChatService.answer("recommend me a good watch");

        // The sunglasses (12) are dropped even though they were retrieved.
        assertEquals(List.of("1", "2"), cardIds(res));
        // The machine-readable marker line is not shown to the shopper.
        assertEquals("Here are two great watches for you.", res.answer());
    }

    @Test
    void keepsTheModelsRecommendationOrder() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any()))
                .thenReturn("The Omega first.\nRECOMMENDED_IDS: 2, 1");

        AssistantResponse res = ragChatService.answer("watch");

        assertEquals(List.of("2", "1"), cardIds(res));
    }

    @Test
    void ignoresIdsThatAreNotAmongTheCandidates() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any()))
                .thenReturn("One pick.\nRECOMMENDED_IDS: 1, 999");

        AssistantResponse res = ragChatService.answer("watch");

        assertEquals(List.of("1"), cardIds(res));
    }

    @Test
    void noneMeansNoCards() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any()))
                .thenReturn("Sorry, nothing here fits.\nRECOMMENDED_IDS: none");

        AssistantResponse res = ragChatService.answer("a spaceship");

        assertTrue(res.products().isEmpty());
        assertEquals("Sorry, nothing here fits.", res.answer());
    }

    @Test
    void fallsBackToAllCandidatesWhenTheMarkerIsMissing() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any()))
                .thenReturn("A friendly answer with no marker line.");

        AssistantResponse res = ragChatService.answer("watch");

        assertEquals(List.of("1", "2", "12"), cardIds(res));
        assertEquals("A friendly answer with no marker line.", res.answer());
    }

    // --- #85: graceful degradation when the chat model is unavailable -------

    @Test
    void returnsRetrievedProductsWhenTheModelFails() {
        retrieveWatchesAndSunglasses();
        when(chatClient.chat(any(), any())).thenThrow(new RuntimeException("503 overloaded"));

        AssistantResponse res = ragChatService.answer("watch");

        // No exception propagates; the shopper still gets the retrieved products...
        assertEquals(List.of("1", "2", "12"), cardIds(res));
        // ...with a friendly note rather than an error.
        assertTrue(res.answer().toLowerCase().contains("briefly busy"), res.answer());
    }

    @Test
    void tellsTheShopperToRetryWhenNothingWasRetrievedAndTheModelFails() {
        when(ragService.search(any(), anyInt())).thenReturn(List.of());
        when(chatClient.chat(any(), any())).thenThrow(new RuntimeException("503 overloaded"));

        AssistantResponse res = ragChatService.answer("watch");

        assertTrue(res.products().isEmpty());
        assertTrue(res.answer().toLowerCase().contains("briefly unavailable"), res.answer());
    }
}
