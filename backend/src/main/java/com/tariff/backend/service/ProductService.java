package com.tariff.backend.service;

import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Product;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  // Add a new product
  public Product addProduct(ProductDTO productDTO) {
    Optional<Product> p = productRepository.findByName(productDTO.getName());
    if (p.isPresent()) {
      throw new BadRequestException("Product already exists");
    }
    
    Product product = new Product();
    product.setDescription(productDTO.getDescription());
    product.setEnabled(productDTO.isEnabled());
    product.setName(productDTO.getName());
    
    return productRepository.save(product);
  }

  // Get a product by ID
  public Product getProductById(UUID productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));
  }

  // Get all products
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  // Update a product
  public Product updateProduct(UUID productId, ProductDTO productDTO) {
    return productRepository.findById(productId).map(product -> {
      product.setName(productDTO.getName());
      product.setDescription(productDTO.getDescription());
      return productRepository.save(product);
    }).orElseThrow(() -> new NotFoundException("Product not found"));
  }

  // Delete a product
  public void deleteProduct(UUID productId, boolean softDelete) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));

    if (softDelete) {
      product.setEnabled(false);
      productRepository.save(product);
    } else {
      productRepository.delete(product);
    }
  }
}