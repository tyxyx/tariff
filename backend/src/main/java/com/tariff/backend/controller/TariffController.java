package com.tariff.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.dto.AddTariffDTO;
import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.service.TariffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "api/tariffs")
public class TariffController {
  private final TariffService tariffService;

  public TariffController(TariffService tariffService) {
    this.tariffService = tariffService;
  }

  @GetMapping("/getByHtsCode")
  public ResponseEntity<List<Tariff>> getTariffByHtsCode(@RequestParam String htsCode) {
    return ResponseEntity.ok().body(tariffService.getTariffsByHtsCode(htsCode));
  }

  @GetMapping()
  public ResponseEntity<List<Tariff>> listTariffs() {
    return ResponseEntity.ok().body(tariffService.listTariff());
  }

  @PostMapping() 
  public ResponseEntity<String> addTariff(@Valid @RequestBody AddTariffDTO addTariffDTO) {
    tariffService.addTariff(addTariffDTO);
    return ResponseEntity.status(201).body("Tariff Created");
  }
// todo change to getmapping use req header/ params
  @PostMapping("/particular-tariff-rate")
  public ResponseEntity<Tariff> particularTariffRate(@Valid @RequestBody ParticularTariffDTO particularTariffDTO) {
    return ResponseEntity.ok().body(tariffService.getParticularTariff(particularTariffDTO));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Tariff> updateTariff(@PathVariable UUID id, @RequestBody Tariff newTariff) {
    return ResponseEntity.ok().body(tariffService.updateTariff(id, newTariff));
  }

  @PostMapping("/{id}/add-product")
  public ResponseEntity<Tariff> addProductToTariff(@PathVariable UUID id, @Valid @RequestBody ProductDTO productDTO) {
    return ResponseEntity.ok().body(tariffService.addProductToTariff(id, productDTO));
  }

  @DeleteMapping("/{id}/remove-product/{productId}")
  public ResponseEntity<Tariff> removeProductFromTariff(@PathVariable UUID id, @PathVariable String productId) {
    return ResponseEntity.ok().body(tariffService.removeProductFromTariff(id, productId));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteTariff(@PathVariable UUID id, @RequestParam(required = false) Boolean softDelete) {
    if (softDelete == null) {
      softDelete = true;
    }
    tariffService.deleteTariff(id, softDelete);
    return ResponseEntity.ok().body(softDelete ? "Tariff soft-deleted" : "Tariff permanently deleted");
  }

  @GetMapping("/{id}")
  public ResponseEntity<Tariff> getTariffById(@PathVariable UUID id) {
    return ResponseEntity.ok().body(tariffService.getTariffById(id));
  }
}
