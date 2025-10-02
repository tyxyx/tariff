package com.tariff.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.Tariff;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, UUID>{
  @Query("""
      SELECT t FROM Tariff t JOIN t.products p
      WHERE t.originCountry = :originCountry
      AND t.destCountry = :destCountry
      AND t.effectiveDate <= :targetDate
      AND (
        t.expiryDate IS NULL
        OR t.expiryDate >= :targetDate
      )
      AND t.enabled
      AND p.name = :productName
      """)
  Optional<Tariff> getTariffFromProductCountriesAndDates(String productName, LocalDate targetDate, String originCountry, String destCountry);

  @Query("""
      SELECT t FROM Tariff t
      WHERE t.HTSCode = :htsCode
      AND t.enabled
      """)
  List<Tariff> getTariffsByHtsCode(String htsCode);

  @Query("""
      SELECT t FROM Tariff t JOIN t.products p
      """)
  List<Tariff> listAll();
}
