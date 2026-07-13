package com.luxshop.shop.controller;

import com.luxshop.shop.dto.AddImageRequest;
import com.luxshop.shop.dto.ProductImageResponse;
import com.luxshop.shop.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Product gallery management. Listing is public (GET /shop/product/** is open);
 * adding and deleting require a Bearer token (admin), enforced by the default
 * "authenticated" rule.
 */
@RestController
@RequestMapping("/shop/product/{productId}/images")
public class ProductImageAdminController {

    private final ProductImageService productImageService;

    public ProductImageAdminController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping
    public List<ProductImageResponse> list(@PathVariable String productId) {
        return productImageService.list(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse add(@PathVariable String productId,
                                    @Valid @RequestBody AddImageRequest request) {
        return productImageService.add(productId, request.url());
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String productId, @PathVariable Long imageId) {
        productImageService.delete(productId, imageId);
    }
}
