package com.luxshop.shop.service;

import com.luxshop.shop.domain.Product;
import com.luxshop.shop.domain.Review;
import com.luxshop.shop.dto.CreateReviewRequest;
import com.luxshop.shop.dto.ReviewResponse;
import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> list(String productId) {
        requireProduct(productId);
        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public ReviewResponse create(String productId, CreateReviewRequest request) {
        Product product = requireProduct(productId);
        Review review = new Review();
        review.setProduct(product);
        review.setAuthorName(request.authorName().trim());
        review.setRating(request.rating());
        review.setComment(request.comment() != null && !request.comment().isBlank()
                ? request.comment().trim() : null);
        review.setCreatedAt(Instant.now());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
    }
}
