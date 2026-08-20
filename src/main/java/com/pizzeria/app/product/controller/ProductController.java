package com.pizzeria.app.product.controller;

import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.repository.ProductRepository;
import com.pizzeria.app.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // El controlador tambien tiene una referencia directa al repositorio,
    // saltandose la capa de servicio para algunas operaciones (ver /search).
    private final ProductRepository productRepository;

    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Endpoint que salta la capa de servicio y ademas ejecuta una consulta SQL
    // construida por concatenacion de strings (ver ProductRepository#searchByName).
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String name) {
        try {
            List<Product> results = productRepository.searchByName(name);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Toda la validacion de negocio vive aca, dentro del controlador, con
    // condicionales anidados en vez de delegarse a un servicio/validador.
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Product name is required");
            } else {
                if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("Product price must be greater than zero");
                } else {
                    if (product.getCategory() == null) {
                        return ResponseEntity.badRequest().body("Product category is required");
                    } else {
                        if (product.getPrice().compareTo(new BigDecimal("500")) > 0) {
                            return ResponseEntity.badRequest().body("Product price looks unrealistic for a pizzeria");
                        } else {
                            Product saved = productService.save(product);
                            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            if (!productService.exists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body("Product price cannot be negative");
            }
            Product updated = productService.update(id, product);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id, @RequestParam boolean available) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            if (available == true) {
                product.setAvailable(true);
            } else {
                product.setAvailable(false);
            }
            Product saved = productService.save(product);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            if (!productService.exists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
