package com.tariff.backend.dto;

import java.time.LocalDate;

public class ParticularTariffDTO {
  private LocalDate date;
  private String originCountry;
  private String destCountry;
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
