package com.tariff.backend.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
  public enum Role { ADMIN, USER }
  
  @Id
  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // This will store the hashed password

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
  private Role role = Role.USER; // Defaults to regular user

  // Constructors, getters, and setters
    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = Role.USER;
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role == null ? Role.USER : role;
    }

  @PrePersist
  void prePersist() {
    if (this.role == null) {
      this.role = Role.USER;
    }
  }

  @ManyToMany(mappedBy = "users")
  private Set<Tariff> tariffs = new HashSet<>();
}
