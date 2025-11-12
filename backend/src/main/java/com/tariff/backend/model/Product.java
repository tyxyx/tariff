package com.tariff.backend.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "product")
@Data
public class Product {
  @Id
  private String HTS_code;
  private String name;
  private String description;
  private boolean enabled = true;

  @ManyToMany(mappedBy = "products")
  @JsonIgnore
  private Set<Tariff> tariffs = new HashSet<>();
}