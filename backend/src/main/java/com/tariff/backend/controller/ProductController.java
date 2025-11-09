package com.tariff.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.model.Product;
import com.tariff.backend.service.ProductService;

import jakarta.validation.Valid;
 

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  // Create a new product
  @PostMapping
  public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductDTO productDTO) {
    Product createdProduct = productService.addProduct(productDTO);
    return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
  }

  // Get a product by ID
  @GetMapping("/{productId}")
  public ResponseEntity<Product> getProductById(@PathVariable String productId) {
    Product product = productService.getProductById(productId);
    return ResponseEntity.ok(product);
  }

  // Get all products
  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  // Update a product
  @PutMapping("/{productId}")
  public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody ProductDTO productDTO) {
    Product product = productService.updateProduct(productId, productDTO);
    return ResponseEntity.ok(product);
  }

  // Delete a product
  @DeleteMapping("/{productId}")
  public ResponseEntity<String> deleteProduct(@PathVariable String productId, @RequestParam(required = false) Boolean softDelete) {
    if (softDelete == null) {
      softDelete = true;
    }
    productService.deleteProduct(productId, softDelete);
    return ResponseEntity.ok().body(softDelete ? "Product soft-deleted" : "Product permanently deleted");
  }
}