package com.luxshop.shop.controller;

import com.luxshop.shop.dto.CreateReviewRequest;
import com.luxshop.shop.dto.ReviewResponse;
import com.luxshop.shop.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public product reviews: anyone can read them or leave one (guest reviews). */
@RestController
@RequestMapping("/shop/product/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> list(@PathVariable String productId) {
        return reviewService.list(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@PathVariable String productId,
                                 @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(productId, request);
    }
}
