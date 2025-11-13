package com.tariff.backend.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "tariff")
@Data
// Exclude collections from equals/hashCode to avoid circular references with Product and User
@EqualsAndHashCode(exclude = {"products", "users"})
@ToString(exclude = {"products", "users"})
public class Tariff {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private LocalDate effectiveDate;
  private LocalDate expiryDate;

  private Double adValoremRate;
  private Double specificRate;

  // whether this tariff is enabled (DB has NOT NULL constraint)
  private boolean enabled = true;

  private long minQuantity;
  private long maxQuantity;

  private boolean userDefined;

  @ManyToOne
  @JoinColumn(name = "origin_country_code", referencedColumnName = "code", nullable = false)
  private Country originCountry;

  @ManyToOne
  @JoinColumn(name = "dest_country_code", referencedColumnName = "code", nullable = false)
  private Country destCountry;

  // Products associated with this tariff (owning side)
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
    name = "tariff_product",
    joinColumns = @JoinColumn(name = "tariff_id"),
    inverseJoinColumns = @JoinColumn(name = "HTS_code")
  )
  @JsonIgnore
  private Set<Product> products = new HashSet<>();


    // Products associated with this tariff (owning side)
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
    name = "tariff_user",
    joinColumns = @JoinColumn(name = "tariff_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> users = new HashSet<>();

  // @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL)
  // private List<Product> products;
}
