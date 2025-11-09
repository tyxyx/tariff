package com.tariff.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.dto.CountryDTO;
import com.tariff.backend.model.Country;
import com.tariff.backend.service.CountryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

  private final CountryService countryService;

  public CountryController(CountryService countryService) {
    this.countryService = countryService;
  }

  // Create a new country
  @PostMapping
  public ResponseEntity<Country> addCountry(@Valid @RequestBody CountryDTO countryDTO) {
    Country created = countryService.addCountry(countryDTO);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  // Get country by code
  @GetMapping("/{code}")
  public ResponseEntity<Country> getCountry(@PathVariable String code) {
    return ResponseEntity.ok(countryService.getCountryByCode(code));
  }

  // Get all countries
  @GetMapping
  public ResponseEntity<List<Country>> getAllCountries() {
    return ResponseEntity.ok(countryService.getAllCountries());
  }

  // Update country (name only)
  @PutMapping("/{code}")
  public ResponseEntity<Country> updateCountry(@PathVariable String code, @Valid @RequestBody CountryDTO countryDTO) {
    return ResponseEntity.ok(countryService.updateCountry(code, countryDTO));
  }

  // Delete country
  @DeleteMapping("/{code}")
  public ResponseEntity<String> deleteCountry(@PathVariable String code) {
    countryService.deleteCountry(code);
    return ResponseEntity.ok("Country deleted");
  }
}
