package com.tariff.backend.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
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

  @ManyToMany(mappedBy = "users")
  private Set<Tariff> tariffs = new HashSet<>();
}
