package com.tariff.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tariff.backend.dto.AddTariffDTO;
import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Product;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.repository.CountryRepository;
import com.tariff.backend.repository.ProductRepository;
import com.tariff.backend.repository.TariffRepository;

import jakarta.transaction.Transactional;

@Service
public class TariffService {
  private final TariffRepository tariffs;
  private final ProductRepository products;
  private final CountryRepository countries;

  public TariffService(TariffRepository tariffs, ProductRepository products, CountryRepository countries) {
    this.tariffs = tariffs;
    this.products = products;
    this.countries = countries;
  }

  // Methods:
  // 1. add in new tariff
  public Tariff addTariff(AddTariffDTO addTariffDTO) {
  // Validate the DTO fields: only compare when expiry is provided
  if (addTariffDTO.getExpiryDate() != null && addTariffDTO.getEffectiveDate().isAfter(addTariffDTO.getExpiryDate())) {
    throw new BadRequestException("Effective date cannot be after expiry date");
  }

  // Check if a tariff with the same HTS code exists for the given origin/dest
  Optional<Tariff> existingTariff = getTariffsByHtsCode(addTariffDTO.getHtscode())
    .stream()
    .filter(tariff -> tariff.getOriginCountry() != null
      && tariff.getOriginCountry().getCode().equals(addTariffDTO.getOriginCountry())
      && tariff.getDestCountry() != null
      && tariff.getDestCountry().getCode().equals(addTariffDTO.getDestCountry()))
    .findFirst();

    if (existingTariff.isPresent()) {
      Tariff t = existingTariff.get();
      System.out.println(t.getExpiryDate());
      if (t.getExpiryDate() != null && (
        addTariffDTO.getEffectiveDate().isBefore(t.getExpiryDate()) || addTariffDTO.getEffectiveDate().equals(t.getExpiryDate())
      )) {
        throw new BadRequestException("New effective date must be " + t.getExpiryDate().plusDays(1) + " onwards");
      }
      t.setExpiryDate(addTariffDTO.getEffectiveDate().minusDays(1));
      tariffs.save(t);
    }

    // Map AddTariffDTO to Tariff entity
  Tariff tariff = new Tariff();
  // Resolve countries from codes
  var origin = countries.findById(addTariffDTO.getOriginCountry())
    .orElseThrow(() -> new BadRequestException("Origin country code not found: " + addTariffDTO.getOriginCountry()));
  var dest = countries.findById(addTariffDTO.getDestCountry())
    .orElseThrow(() -> new BadRequestException("Destination country code not found: " + addTariffDTO.getDestCountry()));

    tariff.setOriginCountry(origin);
    tariff.setDestCountry(dest);
    tariff.setEffectiveDate(addTariffDTO.getEffectiveDate());
    tariff.setExpiryDate(addTariffDTO.getExpiryDate());
  
    // Prefer the new 'rate' field (stored as decimal, e.g. 0.12). If not provided, fall back
    // to the older 'adValoremRate' percentage field (e.g. 12.5 -> 0.125).
    if (addTariffDTO.getRate() != null) {
      tariff.setAdValoremRate(addTariffDTO.getRate());
    } else if (addTariffDTO.getAdValoremRate() != null) {
      tariff.setAdValoremRate(addTariffDTO.getAdValoremRate() / 100.0);
    } else {
      // Defensive default: 0.0 when no rate supplied
      tariff.setAdValoremRate(0.0);
    }
      
    // map optional specific rate from DTO
    if (addTariffDTO.getSpecificRate() != null) {
      tariff.setSpecificRate(addTariffDTO.getSpecificRate());
    }
    // set enabled flag (default true when DTO omitted)
    try {
      tariff.setEnabled(addTariffDTO.isEnabled());
    } catch (Exception e) {
      // defensive: default to true
      tariff.setEnabled(true);
    }

    // Ensure the referenced product (by HTS code) exists and associate it
    Product product = products.findById(addTariffDTO.getHtscode()).orElseGet(() -> {
      Product p = new Product();
      p.setHTS_code(addTariffDTO.getHtscode());
      // If provided, use the first product DTO for metadata
      if (addTariffDTO.getProducts() != null && !addTariffDTO.getProducts().isEmpty()) {
        var meta = addTariffDTO.getProducts().get(0);
        p.setName(meta.getName());
        p.setDescription(meta.getDescription());
        p.setEnabled(meta.isEnabled());
      }
      return products.save(p);
    });

    tariff.getProducts().add(product);

  // Save the new tariff
    return tariffs.save(tariff);
}

  // 2. edit old tariff by id
  public Tariff updateTariff(UUID tariffId, Tariff newTariff) {    
    return tariffs.findById(tariffId).map(tariff -> {
      if (newTariff.getDestCountry() != null) {
        tariff.setDestCountry(newTariff.getDestCountry());
      }
      if (newTariff.getOriginCountry() != null) {
        tariff.setOriginCountry(newTariff.getOriginCountry());
      }
      if (newTariff.getEffectiveDate() != null) {
        tariff.setEffectiveDate(newTariff.getEffectiveDate());
      }
      if (newTariff.getExpiryDate() != null) {
        tariff.setExpiryDate(newTariff.getExpiryDate());
      }
      if (newTariff.getAdValoremRate() != null) {
        tariff.setAdValoremRate(newTariff.getAdValoremRate());
      }
      if (newTariff.getSpecificRate() != null) {
        tariff.setSpecificRate(newTariff.getSpecificRate());
      }
      return tariffs.save(tariff);
    }).orElseThrow(() -> new NotFoundException("Tariff not found"));
  }

  // 2a. add product into tariff
  @Transactional
  public Tariff addProductToTariff(UUID tariffId, ProductDTO productDTO) {
      // Check if a product with the same name already exists
      Optional<Product> existingProduct = products.findByName(productDTO.getName());
      Product product;

      if (existingProduct.isPresent()) {
          product = existingProduct.get();
      } else {
          // Create a new product if it doesn't exist
          product = new Product();
          product.setDescription(productDTO.getDescription());
          product.setEnabled(productDTO.isEnabled());
          product.setName(productDTO.getName());
          products.save(product);
      }

      // Find the tariff
      Tariff tariff = tariffs.findById(tariffId)
          .orElseThrow(() -> new NotFoundException("Tariff not found"));

      // Check if the product is already associated with the tariff
      if (tariff.getProducts().contains(product)) {
          throw new BadRequestException(String.format("Product '%s' already exists in the tariff", product.getName()));
      }

      // Add the product to the tariff
      tariff.getProducts().add(product);
      return tariffs.save(tariff);
  }

  // 2b. remove product from tariff
  public Tariff removeProductFromTariff(UUID tariffId, String productId) {
    Tariff tariff = tariffs.findById(tariffId)
        .orElseThrow(() -> new NotFoundException("Tariff not found"));

    Product product = products.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));

    if (!tariff.getProducts().remove(product)) {
      throw new BadRequestException("Product does not exist in the tariff");
    }

    return tariffs.save(tariff);
  }

  // 3. delete tariff (soft & hard deletion)
  public void deleteTariff(UUID tariffId, boolean softDelete) {
    Tariff tariff = tariffs.findById(tariffId)
        .orElseThrow(() -> new NotFoundException("Tariff not found"));

    if (softDelete) {
      // Soft-delete by moving expiry date to before effective date (or now - 1 day)
      LocalDate base = tariff.getEffectiveDate() != null ? tariff.getEffectiveDate() : LocalDate.now();
      tariff.setExpiryDate(base.minusDays(1));
      tariffs.save(tariff);
    } else {
      tariffs.delete(tariff);
    }
  }

  // 4. search the tariff by htscode
  public List<Tariff> getTariffsByHtsCode(String htsCode) {
    return tariffs.getTariffsByHtsCode(htsCode);
  }

  // 4a. search tariff by id
  public Tariff getTariffById(UUID id) {
    Optional<Tariff> t = tariffs.findById(id);
    if (!t.isPresent()) {
      throw new NotFoundException(String.format("Tariff with id %s not found", id));
    }
    return t.get();
  }

  // 4b. list all tariff
  public List<Tariff> listTariff() {
    return tariffs.listAll();
  }

  // 5. search the tariff based on product, a particular date, country of origin and dest
  public Tariff getParticularTariff(ParticularTariffDTO dto) {
    Optional<Tariff> tar =  tariffs.getTariffFromProductCountriesAndDates(dto.getProductName(), dto.getDate(), dto.getOriginCountry(), dto.getDestCountry());

    if (!tar.isPresent()) {
      throw new NotFoundException("No tariff found");
    }

    return tar.get();
  }
}
