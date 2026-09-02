package com.pizzeria.app.product.controller;

import com.pizzeria.app.product.dto.ProductRequest;
import com.pizzeria.app.product.dto.ProductResponse;
import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        List<ProductResponse> results = productService.search(name).stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product saved = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, request);
        return ResponseEntity.ok(ProductResponse.from(updated));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProductResponse> toggleAvailability(@PathVariable Long id, @RequestParam boolean available) {
        Product updated = productService.updateAvailability(id, available);
        return ResponseEntity.ok(ProductResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
