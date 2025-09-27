package com.tariff.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.service.TariffService;

@RestController
@RequestMapping(path = "api/tariffs")
public class TariffController {
  private final TariffService tariffService;

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(NotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  public TariffController(TariffService tariffService) {
    this.tariffService = tariffService;
  }

  @PostMapping("/add") 
  public ResponseEntity<String> addTariff(@RequestBody Tariff tariff) {
    try {
      tariffService.addTariff(tariff);
      return ResponseEntity.status(201).body("Tariff Created");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Unable to add a new tariff.");
    }
  }

  @PostMapping("/get-particular-tariff-rate")
  public ResponseEntity<Tariff> getParticularTariffRate(@RequestBody ParticularTariffDTO dto) {
    return ResponseEntity.ok().body(tariffService.getParticularTariff(dto));
  }
}
