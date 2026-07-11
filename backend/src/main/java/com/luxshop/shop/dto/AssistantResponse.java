package com.luxshop.shop.dto;

import java.util.List;

/** The assistant's reply plus the catalog products it drew on (for cards). */
public record AssistantResponse(String answer, List<ProductResponse> products) {
}
