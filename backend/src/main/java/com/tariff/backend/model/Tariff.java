package com.tariff.backend.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tariff")
@Data
public class Tariff {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String HTSCode;
  private String originCountry;
  private String destCountry;
  private LocalDate tariffEffectiveDate;
  private LocalDate tariffExpiryDate;
  private double tariffRate;
  private String productName;

  // @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL)
  // private List<Product> products;
}
