package com.tariff.backend.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddTariffDTO {
  @NotNull(message = "Origin country is required")
  @Size(min = 1, message = "Origin country cannot be empty")
  private String originCountry;

  @NotNull(message = "Destination country is required")
  @Size(min = 1, message = "Destination country cannot be empty")
  private String destCountry;

  @NotNull(message = "Effective date is required")
  private LocalDate effectiveDate;

  private LocalDate expiryDate;

  @NotNull(message = "Rate is required")
  private Double rate;

  private Boolean enabled;

  @NotNull(message = "HTS code is required")
  @Size(min = 1, message = "HTS code cannot be empty")
  private String htscode;

  @NotNull(message = "Products list is required")
  @Valid
  private List<ProductDTO> products;

  public String getOriginCountry() {
    return originCountry;
  }

  public void setOriginCountry(String originCountry) {
    this.originCountry = originCountry;
  }

  public String getDestCountry() {
    return destCountry;
  }

  public void setDestCountry(String destCountry) {
    this.destCountry = destCountry;
  }

  public LocalDate getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(LocalDate effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }

  public Double getRate() {
    return rate;
  }

  public void setRate(Double rate) {
    this.rate = rate;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getHtscode() {
    return htscode;
  }

  public void setHtscode(String htscode) {
    this.htscode = htscode;
  }

  public List<ProductDTO> getProducts() {
    return products;
  }

  public void setProducts(List<ProductDTO> products) {
    this.products = products;
  }
}
