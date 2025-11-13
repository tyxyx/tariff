package com.tariff.backend.repository;

import com.tariff.backend.model.Country;
import com.tariff.backend.model.Product;
import com.tariff.backend.model.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TariffRepositoryTest {

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Country usaCountry;
    private Country chinaCountry;
    private Product enabledProduct;
    private Product disabledProduct;

    @BeforeEach
    void setUp() {
        // Create countries
        usaCountry = new Country();
        usaCountry.setCode("US");
        usaCountry.setName("United States");
        entityManager.persist(usaCountry);

        chinaCountry = new Country();
        chinaCountry.setCode("CN");
        chinaCountry.setName("China");
        entityManager.persist(chinaCountry);

        // Create products
        enabledProduct = new Product();
        enabledProduct.setHTS_code("1234.56.78");
        enabledProduct.setName("Test Product");
        enabledProduct.setDescription("Test Description");
        enabledProduct.setEnabled(true);
        entityManager.persist(enabledProduct);

        disabledProduct = new Product();
        disabledProduct.setHTS_code("9876.54.32");
        disabledProduct.setName("Disabled Product");
        disabledProduct.setDescription("Disabled Description");
        disabledProduct.setEnabled(false);
        entityManager.persist(disabledProduct);

        entityManager.flush();
    }

    @Test
    void getTariffFromProductCountriesAndDates_withValidData_shouldReturnTariff() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(LocalDate.of(2024, 12, 31));
        tariff.setAdValoremRate(10.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(enabledProduct);
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        Optional<Tariff> result = tariffRepository.getTariffFromProductCountriesAndDates(
                "Test Product",
                LocalDate.of(2024, 6, 15),
                "CN",
                "US"
        );

        assertTrue(result.isPresent());
        assertEquals(tariff.getId(), result.get().getId());
        assertEquals(10.0, result.get().getAdValoremRate());
    }

    @Test
    void getTariffFromProductCountriesAndDates_withDateBeforeEffective_shouldReturnEmpty() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(LocalDate.of(2024, 12, 31));
        tariff.setAdValoremRate(10.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(enabledProduct);
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        // When - query with date before effective date
        Optional<Tariff> result = tariffRepository.getTariffFromProductCountriesAndDates(
                "1234.56.78",
                LocalDate.of(2023, 12, 31),
                "CN",
                "US"
        );

        assertFalse(result.isPresent());
    }

    @Test
    void getTariffFromProductCountriesAndDates_withDateAfterExpiry_shouldReturnEmpty() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(LocalDate.of(2024, 12, 31));
        tariff.setAdValoremRate(10.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(enabledProduct);
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        // When - query with date after expiry date
        Optional<Tariff> result = tariffRepository.getTariffFromProductCountriesAndDates(
                "1234.56.78",
                LocalDate.of(2025, 1, 1),
                "CN",
                "US"
        );

        assertFalse(result.isPresent());
    }

    @Test
    void getTariffFromProductCountriesAndDates_withNullExpiryDate_shouldReturnTariff() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(null); // No expiry date
        tariff.setAdValoremRate(15.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(enabledProduct);
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        // When - query with future date (should still work since no expiry)
        Optional<Tariff> result = tariffRepository.getTariffFromProductCountriesAndDates(
                "Test Product",
                LocalDate.of(2025, 6, 15),
                "CN",
                "US"
        );

        assertTrue(result.isPresent());
        assertEquals(15.0, result.get().getAdValoremRate());
    }

    @Test
    void getTariffFromProductCountriesAndDates_withDisabledProduct_shouldReturnEmpty() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(LocalDate.of(2024, 12, 31));
        tariff.setAdValoremRate(10.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(disabledProduct); // Using disabled product
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        Optional<Tariff> result = tariffRepository.getTariffFromProductCountriesAndDates(
                "9876.54.32",
                LocalDate.of(2024, 6, 15),
                "CN",
                "US"
        );

        assertFalse(result.isPresent());
    }

    @Test
    void getTariffsByHtsCode_withEnabledProduct_shouldReturnTariffs() {
        Tariff tariff1 = new Tariff();
        tariff1.setOriginCountry(chinaCountry);
        tariff1.setDestCountry(usaCountry);
        tariff1.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff1.setAdValoremRate(10.0);
        tariff1.setSpecificRate(0.0);
        tariff1.setMinQuantity(0);
        tariff1.setMaxQuantity(1000);
        tariff1.setUserDefined(false);
        
        Set<Product> products1 = new HashSet<>();
        products1.add(enabledProduct);
        tariff1.setProducts(products1);
        
        Tariff tariff2 = new Tariff();
        tariff2.setOriginCountry(usaCountry);
        tariff2.setDestCountry(chinaCountry);
        tariff2.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff2.setAdValoremRate(5.0);
        tariff2.setSpecificRate(0.0);
        tariff2.setMinQuantity(0);
        tariff2.setMaxQuantity(1000);
        tariff2.setUserDefined(false);
        
        Set<Product> products2 = new HashSet<>();
        products2.add(enabledProduct);
        tariff2.setProducts(products2);
        
        entityManager.persist(tariff1);
        entityManager.persist(tariff2);
        entityManager.flush();

        List<Tariff> results = tariffRepository.getTariffsByHtsCode("1234.56.78");

        assertEquals(2, results.size());
    }

    @Test
    void getTariffsByHtsCode_withDisabledProduct_shouldReturnEmpty() {
        Tariff tariff = new Tariff();
        tariff.setOriginCountry(chinaCountry);
        tariff.setDestCountry(usaCountry);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setAdValoremRate(10.0);
        tariff.setSpecificRate(0.0);
        tariff.setMinQuantity(0);
        tariff.setMaxQuantity(1000);
        tariff.setUserDefined(false);
        
        Set<Product> products = new HashSet<>();
        products.add(disabledProduct);
        tariff.setProducts(products);
        
        entityManager.persist(tariff);
        entityManager.flush();

        List<Tariff> results = tariffRepository.getTariffsByHtsCode("9876.54.32");

        assertTrue(results.isEmpty());
    }

    @Test
    void getTariffsByHtsCode_withNonExistentHtsCode_shouldReturnEmpty() {
        List<Tariff> results = tariffRepository.getTariffsByHtsCode("0000.00.00");

        assertTrue(results.isEmpty());
    }

    @Test
    void listAll_shouldReturnAllTariffsWithProducts() {
        Tariff tariff1 = new Tariff();
        tariff1.setOriginCountry(chinaCountry);
        tariff1.setDestCountry(usaCountry);
        tariff1.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff1.setAdValoremRate(10.0);
        tariff1.setSpecificRate(0.0);
        tariff1.setMinQuantity(0);
        tariff1.setMaxQuantity(1000);
        tariff1.setUserDefined(false);
        
        Set<Product> products1 = new HashSet<>();
        products1.add(enabledProduct);
        tariff1.setProducts(products1);
        
        Tariff tariff2 = new Tariff();
        tariff2.setOriginCountry(usaCountry);
        tariff2.setDestCountry(chinaCountry);
        tariff2.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff2.setAdValoremRate(5.0);
        tariff2.setSpecificRate(0.0);
        tariff2.setMinQuantity(0);
        tariff2.setMaxQuantity(1000);
        tariff2.setUserDefined(false);
        
        Set<Product> products2 = new HashSet<>();
        products2.add(disabledProduct);
        tariff2.setProducts(products2);
        
        entityManager.persist(tariff1);
        entityManager.persist(tariff2);
        entityManager.flush();

        List<Tariff> results = tariffRepository.listAll();

        assertEquals(2, results.size());
        
        // Verify products are fetched (no lazy loading exception)
        results.forEach(tariff -> {
            assertNotNull(tariff.getProducts());
            assertFalse(tariff.getProducts().isEmpty());
        });
    }

    @Test
    void listAll_withNoTariffs_shouldReturnEmptyList() {
        List<Tariff> results = tariffRepository.listAll();

        assertTrue(results.isEmpty());
    }
}
