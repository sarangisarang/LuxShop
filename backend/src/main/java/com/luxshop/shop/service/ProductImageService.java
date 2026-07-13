package com.luxshop.shop.service;

import com.luxshop.shop.domain.Product;
import com.luxshop.shop.domain.ProductImage;
import com.luxshop.shop.dto.ProductImageResponse;
import com.luxshop.shop.repository.ProductImageRepository;
import com.luxshop.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductImageService {

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository imageRepository, ProductRepository productRepository) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> list(String productId) {
        requireProduct(productId);
        return imageRepository.findByProduct_IdOrderByPositionAsc(productId)
                .stream().map(ProductImageResponse::from).toList();
    }

    @Transactional
    public ProductImageResponse add(String productId, String url) {
        Product product = requireProduct(productId);
        int nextPosition = imageRepository.findByProduct_IdOrderByPositionAsc(productId)
                .stream().mapToInt(ProductImage::getPosition).max().orElse(0) + 1;
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url.trim());
        image.setPosition(nextPosition);
        return ProductImageResponse.from(imageRepository.save(image));
    }

    @Transactional
    public void delete(String productId, Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + imageId));
        if (image.getProduct() == null || !productId.equals(image.getProduct().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image does not belong to product " + productId);
        }
        imageRepository.delete(image);
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
    }
}
