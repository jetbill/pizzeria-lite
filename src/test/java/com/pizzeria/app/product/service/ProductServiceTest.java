package com.pizzeria.app.product.service;

import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.entity.ProductCategory;
import com.pizzeria.app.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    private Product margherita;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);

        margherita = new Product();
        margherita.setId(1L);
        margherita.setName("Margherita Pizza");
        margherita.setCategory(ProductCategory.PIZZA);
        margherita.setPrice(new BigDecimal("8.99"));
        margherita.setAvailable(true);
    }

    @Test
    void findById_devuelveElProductoCuandoExiste() {
        when(productRepository.findById(1L)).thenReturn(margherita);

        Product result = productService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Margherita Pizza");
        verify(productRepository).findById(1L);
    }

    @Test
    void findAll_devuelveTodosLosProductosDelRepositorio() {
        Product pepperoni = new Product();
        pepperoni.setId(2L);
        pepperoni.setName("Pepperoni Pizza");
        pepperoni.setCategory(ProductCategory.PIZZA);
        pepperoni.setPrice(new BigDecimal("10.99"));
        pepperoni.setAvailable(true);

        when(productRepository.findAll()).thenReturn(List.of(margherita, pepperoni));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(2).extracting(Product::getName)
                .containsExactly("Margherita Pizza", "Pepperoni Pizza");
    }

    @Test
    void save_delegaElGuardadoAlRepositorio() {
        when(productRepository.save(margherita)).thenReturn(margherita);

        Product result = productService.save(margherita);

        assertThat(result).isEqualTo(margherita);
        verify(productRepository).save(margherita);
    }

    @Test
    void delete_delegaLaEliminacionAlRepositorioPorId() {
        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void exists_devuelveFalseCuandoElProductoNoExiste() {
        when(productRepository.existsById(99L)).thenReturn(false);

        boolean result = productService.exists(99L);

        assertThat(result).isFalse();
    }
}
