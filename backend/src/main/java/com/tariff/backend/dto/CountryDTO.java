package com.tariff.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CountryDTO {

  @NotBlank(message = "Country code is required")
  @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
  private String code;

  @NotBlank(message = "Country name is required")
  @Size(min = 1, max = 100, message = "Country name must be between 1 and 100 characters")
  private String name;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
