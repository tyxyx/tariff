package com.tariff.backend.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.Tariff;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, UUID>{
  @Query("""
      SELECT t FROM Tariff t 
      WHERE t.productName = ?1
      AND t.originCountry = ?3
      AND t.destCountry = ?4
      AND t.tariffEffectiveDate <= ?2
      AND (
        t.tariffExpiryDate IS NULL
        OR t.tariffExpiryDate >= ?2
      )
      """)
  Optional<Tariff> getTariffFromProductCountriesAndDates(String productName, LocalDate targetDate, String originCountry, String destCountry);
}
