package com.tariff.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDTO {

  @NotNull(message = "Product name is required")
  @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
  private String name;

  @NotNull(message = "Product description is required")
  @Size(max = 255, message = "Product description cannot exceed 255 characters")
  private String description;

  private Boolean enabled;

  // Getters and Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnabled() {
    // Null-safe: default to false when not provided
    return Boolean.TRUE.equals(enabled);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}