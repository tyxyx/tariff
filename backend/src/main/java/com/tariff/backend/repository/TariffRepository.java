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
      WHERE t.originCountry.code = :originCountry
      AND t.destCountry.code = :destCountry
      AND t.effectiveDate <= :targetDate
      AND (
        t.expiryDate IS NULL
        OR t.expiryDate >= :targetDate
      )
      AND p.enabled = true
      AND p.HTS_code = :productName
      """)
  Optional<Tariff> getTariffFromProductCountriesAndDates(String productName, LocalDate targetDate, String originCountry, String destCountry);

  @Query("""
    SELECT DISTINCT t FROM Tariff t JOIN t.products p
    WHERE p.HTS_code = :htsCode
    AND p.enabled = true
      """)
  List<Tariff> getTariffsByHtsCode(String htsCode);

  @Query("""
    SELECT DISTINCT t FROM Tariff t LEFT JOIN FETCH t.products p
      """)
  List<Tariff> listAll();
}
