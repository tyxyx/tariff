package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Product;
import com.tariff.backend.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void addProductShouldPersistWhenNameUnique() {
        ProductDTO dto = buildProductDTO();
        when(productRepository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product saved = productService.addProduct(dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product persisted = captor.getValue();

        assertThat(persisted.getName()).isEqualTo(dto.getName());
        assertThat(persisted.getDescription()).isEqualTo(dto.getDescription());
        assertThat(persisted.isEnabled()).isEqualTo(dto.isEnabled());
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void addProductShouldThrowWhenNameExists() {
        ProductDTO dto = buildProductDTO();
        when(productRepository.findByName(dto.getName())).thenReturn(Optional.of(new Product()));

        assertThatThrownBy(() -> productService.addProduct(dto))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Product already exists");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductByIdShouldReturnProduct() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(id);

        assertThat(found).isSameAs(product);
    }

    @Test
    void getProductByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Product not found");
    }

    @Test
    void getAllProductsShouldReturnRepositoryResult() {
        List<Product> products = List.of(buildProduct(), buildProduct());
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).isEqualTo(products);
    }

    @Test
    void updateProductShouldPersistChanges() {
        UUID id = UUID.randomUUID();
        Product existing = buildProduct();
        existing.setId(id);
        ProductDTO dto = new ProductDTO();
        dto.setName("Updated Name");
        dto.setDescription("Updated Description");

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(id, dto);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        verify(productRepository).save(existing);
    }

    @Test
    void updateProductShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        ProductDTO dto = buildProductDTO();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Product not found");
    }

    @Test
    void deleteProductShouldSoftDeleteWhenRequested() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(id, true);

        assertThat(product.isEnabled()).isFalse();
        verify(productRepository).save(product);
        verify(productRepository, never()).delete(product);
    }

    @Test
    void deleteProductShouldHardDeleteWhenSoftDeleteFalse() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.deleteProduct(id, false);

        verify(productRepository).delete(product);
        verify(productRepository, never()).save(any(Product.class));
    }

    private ProductDTO buildProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Widget");
        dto.setDescription("Widget Description");
        dto.setEnabled(true);
        return dto;
    }

    private Product buildProduct() {
        Product product = new Product();
        product.setName("Widget");
        product.setDescription("Widget Description");
        product.setEnabled(true);
        return product;
    }
}
