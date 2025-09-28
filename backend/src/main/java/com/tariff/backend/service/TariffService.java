package com.tariff.backend.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.repository.TariffRepository;

@Service
public class TariffService {
  private final TariffRepository tariffs;

  public TariffService(TariffRepository tariffs) {
    this.tariffs = tariffs;
  }

  // Methods:
  // 1. add in new tariff
  public Tariff addTariff(Tariff tariff) {
    // check if tariff with the same code exists

    // check if the origin & dest countries matches

    // check that the start and end dates do not overlap

    // add in new record
    return tariffs.save(tariff);
  }

  // 2. edit old tariff by id
  public Tariff updateTariff(UUID tariffId, Tariff newTariff) {    
    return tariffs.findById(tariffId).map(tariff -> {
      tariff.setDestCountry(newTariff.getDestCountry());
      tariff.setHTSCode(newTariff.getHTSCode());
      tariff.setOriginCountry(newTariff.getOriginCountry());
      tariff.setTariffEffectiveDate(newTariff.getTariffEffectiveDate());
      tariff.setTariffExpiryDate(newTariff.getTariffExpiryDate());
      tariff.setTariffRate(newTariff.getTariffRate());
      tariff.setProductName(newTariff.getProductName());
      // tariff.setProducts(newTariff.getProducts());
      return tariffs.save(tariff);
    }).orElseThrow(() -> new NotFoundException("Tariff not found"));
  }

  // 3. delete tariff (soft & hard deletion)


  // 4. search the tariff by htscode


  // 5. search the tariff based on product, a particular date, country of origin and dest
  public Tariff getParticularTariff(ParticularTariffDTO dto) {
    Optional<Tariff> tar =  tariffs.getTariffFromProductCountriesAndDates(dto.getProductName(), dto.getDate(), dto.getOriginCountry(), dto.getDestCountry());

    if (!tar.isPresent()) {
      throw new NotFoundException("No tariff found");
    }

    return tar.get();
  }


}
