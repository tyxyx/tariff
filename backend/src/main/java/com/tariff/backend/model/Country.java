package com.tariff.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name="Country")
@Data
public class Country {
  @Id
  private String code;
  private String name;
}
