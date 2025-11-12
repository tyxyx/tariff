package com.tariff.backend.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@Table(name = "users")
@EqualsAndHashCode(exclude = {"tariffs"})
@ToString(exclude = {"tariffs"})
public class User implements UserDetails{
  public enum Role { SUPER_ADMIN, ADMIN, USER }
  
  @Id
  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password; // This will store the hashed password

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
  private Role role = Role.USER; // Defaults to regular user

    // change to protected, just in case.
    protected User() {}

  // Constructors, getters, and setters
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
  @JsonIgnore // avoid serializing tariffs on user to prevent cycles
  private Set<Tariff> tariffs = new HashSet<>();

    // Setters
    public void upgradeRole() {
        if (this.role == Role.USER) {
            this.role = Role.ADMIN;
        }
    }

    public void downgradeRole() {
        if (this.role == Role.ADMIN) {
            this.role = Role.USER;
        }
    }
    
    // Please DO NOT REMOVE, this handles the authentication for JWT,
    // if removed can run, but JWT would not be used.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DO NOT REMOVE: Prefixes with "ROLE_" for Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // DO NOT REMOVE: it's for overriding UserDetail Implement
    // These are generated or looked up at the time the user logs in or is authenticated by a token.
    // Not required to store in database, I set everything to true since we not using it.
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
