package com.luxshop.shop.controller;

import com.luxshop.shop.dto.AssistantRequest;
import com.luxshop.shop.dto.AssistantResponse;
import com.luxshop.shop.service.RagChatService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public RAG shopping assistant. Postgres profile only (needs the RAG stack). */
@RestController
@RequestMapping("/shop/assistant")
@Profile("postgres")
public class AssistantController {

    private final RagChatService ragChatService;

    public AssistantController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping
    public AssistantResponse ask(@Valid @RequestBody AssistantRequest request) {
        return ragChatService.answer(request.message());
    }
}
