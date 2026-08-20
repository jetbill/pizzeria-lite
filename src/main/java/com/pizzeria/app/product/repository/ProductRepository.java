package com.pizzeria.app.product.repository;

import com.pizzeria.app.product.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

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

    // Busqueda construida por concatenacion de strings: vulnerable a inyeccion SQL.
    // Deberia usarse una Query Method de Spring Data o una @Query parametrizada.
    @SuppressWarnings("unchecked")
    public List<Product> searchByName(String name) {
        String sql = "SELECT * FROM products WHERE name ILIKE '%" + name + "%'";
        Query query = entityManager.createNativeQuery(sql, Product.class);
        return query.getResultList();
    }
}
