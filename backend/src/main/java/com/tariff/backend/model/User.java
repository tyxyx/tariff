package com.tariff.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
  
  @Id
  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // This will store the hashed password

  // Constructors, getters, and setters
    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
