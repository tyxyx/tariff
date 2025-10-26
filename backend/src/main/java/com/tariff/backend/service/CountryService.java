package com.tariff.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tariff.backend.dto.CountryDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Country;
import com.tariff.backend.repository.CountryRepository;

@Service
public class CountryService {

  private final CountryRepository countryRepository;

  public CountryService(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  // Create a new country
  public Country addCountry(CountryDTO countryDTO) {
    String code = countryDTO.getCode();
    if (countryRepository.existsById(code)) {
      throw new BadRequestException("Country already exists");
    }

    Country c = new Country();
    c.setCode(code);
    c.setName(countryDTO.getName());
    return countryRepository.save(c);
  }

  // Get a country by code
  public Country getCountryByCode(String code) {
    return countryRepository.findById(code)
        .orElseThrow(() -> new NotFoundException("Country not found"));
  }

  // List all countries
  public List<Country> getAllCountries() {
    return countryRepository.findAll();
  }

  // Update a country name by code
  public Country updateCountry(String code, CountryDTO countryDTO) {
    return countryRepository.findById(code).map(existing -> {
      existing.setName(countryDTO.getName());
      return countryRepository.save(existing);
    }).orElseThrow(() -> new NotFoundException("Country not found"));
  }

  // Delete a country (permanent)
  public void deleteCountry(String code) {
    Country c = countryRepository.findById(code)
        .orElseThrow(() -> new NotFoundException("Country not found"));
    countryRepository.delete(c);
  }
}
