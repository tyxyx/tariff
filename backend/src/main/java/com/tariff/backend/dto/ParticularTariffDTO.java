package com.tariff.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ParticularTariffDTO {
  @NotNull(message = "A date is required")
  private LocalDate date;

  @NotNull(message = "Country of origin is required")
  @Size(min = 1, message = "Country of origin must not be empty string")
  private String originCountry;
  
  @NotNull(message = "Destination country is required")
  @Size(min = 1, message = "Destination country must not be empty string")
  private String destCountry;

  
  @NotNull(message = "Product name is required")
  @Size(min = 1, message = "Product name must not be empty string")
  private String productName;

  public LocalDate getDate() {
    return date;
  }
  public void setDate(LocalDate date) {
    this.date = date;
  }
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
  public String getProductName() {
    return productName;
  }
  public void setProductName(String productName) {
    this.productName = productName;
  }
}
