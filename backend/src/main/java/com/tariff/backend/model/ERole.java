package com.tariff.backend.model;

// Manage Roles
public enum ERole {
  ROLE_USER("user"),
  ROLE_ADMIN("admin");

  public final String name;

  ERole(String name) {
    this.name = name;
  }
}
