package com.tariff.backend.dto;

public class UserLoginDTO {

  private String token;
  private long expiresIn;

  // Constructor
  public UserLoginDTO(String token, long expiresIn) {
    this.token = token;
    this.expiresIn = expiresIn;
  }

  // Getters and setters
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }
}
