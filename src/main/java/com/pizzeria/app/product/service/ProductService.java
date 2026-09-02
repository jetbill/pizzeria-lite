package com.pizzeria.app.product.service;

import com.pizzeria.app.common.exception.BusinessValidationException;
import com.pizzeria.app.product.dto.ProductRequest;
import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.exception.ProductNotFoundException;
import com.pizzeria.app.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BigDecimal maxPrice;

    public ProductService(ProductRepository productRepository,
                           @Value("${product.max-price}") BigDecimal maxPrice) {
        this.productRepository = productRepository;
        this.maxPrice = maxPrice;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public List<Product> search(String name) {
        return productRepository.searchByName(name);
    }

    public Product create(ProductRequest request) {
        validatePrice(request.price());

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setAvailable(request.available());

        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product existing = findById(id);
        validatePrice(request.price());

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setCategory(request.category());
        existing.setPrice(request.price());
        existing.setAvailable(request.available());

        return productRepository.save(existing);
    }

    public Product updateAvailability(Long id, boolean available) {
        Product product = findById(id);
        product.setAvailable(available);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        findById(id);
        productRepository.deleteById(id);
    }

    private void validatePrice(BigDecimal price) {
        if (price.compareTo(maxPrice) > 0) {
            throw new BusinessValidationException("Product price looks unrealistic for a pizzeria");
        }
    }
}
