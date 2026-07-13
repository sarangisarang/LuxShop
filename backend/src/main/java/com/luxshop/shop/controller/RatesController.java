package com.luxshop.shop.controller;

import com.luxshop.shop.dto.RatesResponse;
import com.luxshop.shop.service.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public live exchange rates (base GEL) for the storefront currency switcher. */
@RestController
@RequestMapping("/shop/rates")
public class RatesController {

    private final ExchangeRateService exchangeRateService;

    public RatesController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public RatesResponse rates() {
        return exchangeRateService.getRates();
    }
}
