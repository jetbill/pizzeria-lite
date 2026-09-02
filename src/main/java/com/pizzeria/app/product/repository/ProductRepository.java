package com.pizzeria.app.product.repository;

import com.pizzeria.app.product.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepository(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    public Product findById(Long id) {
        return jpaRepository.findById(id).orElse(null);
    }

    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    public List<Product> searchByName(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name);
    }
}
