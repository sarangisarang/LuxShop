package com.luxshop.shop.dto;

import java.util.Map;

/** Exchange rates for converting the base (GEL) prices into other currencies. */
public record RatesResponse(String base, Map<String, Double> rates, String updated) {
}
