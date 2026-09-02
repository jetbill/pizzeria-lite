package com.pizzeria.app.product.service;

import com.pizzeria.app.common.exception.BusinessValidationException;
import com.pizzeria.app.product.dto.ProductRequest;
import com.pizzeria.app.product.entity.Product;
import com.pizzeria.app.product.entity.ProductCategory;
import com.pizzeria.app.product.exception.ProductNotFoundException;
import com.pizzeria.app.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    private Product margherita;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, new BigDecimal("500"));

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

        assertThat(result.getName()).isEqualTo("Margherita Pizza");
    }

    @Test
    void findById_lanzaExcepcionCuandoNoExiste() {
        when(productRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
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
    void create_guardaElProductoCuandoElPrecioEsValido() {
        ProductRequest request = new ProductRequest("Hawaiana", "Piña y jamón", ProductCategory.PIZZA,
                new BigDecimal("11.50"), true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Hawaiana");
        assertThat(result.getPrice()).isEqualByComparingTo("11.50");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_rechazaUnPrecioPorEncimaDelMaximoConfigurado() {
        ProductRequest request = new ProductRequest("Pizza de oro", null, ProductCategory.PIZZA,
                new BigDecimal("501"), true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessValidationException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_delegaLaEliminacionAlRepositorioPorId() {
        when(productRepository.findById(1L)).thenReturn(margherita);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_lanzaExcepcionCuandoElProductoNoExiste() {
        when(productRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).deleteById(any());
    }
}
