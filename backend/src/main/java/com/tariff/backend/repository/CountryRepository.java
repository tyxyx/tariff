package com.tariff.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
}
