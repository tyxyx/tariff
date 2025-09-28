package com.tariff.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>{
  @Query("""
      SELECT p FROM Product p
      WHERE p.name = :name
      AND p.enabled
      """)
  Optional<Product> findByName(String name);
}
