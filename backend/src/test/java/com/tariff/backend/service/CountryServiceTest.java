package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tariff.backend.dto.CountryDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Country;
import com.tariff.backend.repository.CountryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    private CountryService countryService;

    @BeforeEach
    void setUp() {
        countryService = new CountryService(countryRepository);
    }

    @Test
    void addCountryShouldPersistWhenCodeUnique() {
        CountryDTO dto = new CountryDTO();
        dto.setCode("US");
        dto.setName("United States");
        
        when(countryRepository.existsById("US")).thenReturn(false);
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Country saved = countryService.addCountry(dto);

        ArgumentCaptor<Country> captor = ArgumentCaptor.forClass(Country.class);
        verify(countryRepository).save(captor.capture());
        Country persisted = captor.getValue();

        assertThat(persisted.getCode()).isEqualTo("US");
        assertThat(persisted.getName()).isEqualTo("United States");
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void addCountryShouldThrowWhenCodeExists() {
        CountryDTO dto = new CountryDTO();
        dto.setCode("US");
        dto.setName("United States");
        
        when(countryRepository.existsById("US")).thenReturn(true);

        assertThatThrownBy(() -> countryService.addCountry(dto))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Country already exists");
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void getCountryByCodeShouldReturnCountry() {
        Country country = new Country();
        country.setCode("CN");
        country.setName("China");
        
        when(countryRepository.findById("CN")).thenReturn(Optional.of(country));

        Country found = countryService.getCountryByCode("CN");

        assertThat(found).isSameAs(country);
        assertThat(found.getCode()).isEqualTo("CN");
        assertThat(found.getName()).isEqualTo("China");
    }

    @Test
    void getCountryByCodeShouldThrowWhenNotFound() {
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.getCountryByCode("XX"))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Country not found");
    }

    @Test
    void getAllCountriesShouldReturnAllCountries() {
        Country us = new Country();
        us.setCode("US");
        us.setName("United States");
        
        Country cn = new Country();
        cn.setCode("CN");
        cn.setName("China");
        
        List<Country> countries = List.of(us, cn);
        when(countryRepository.findAll()).thenReturn(countries);

        List<Country> result = countryService.getAllCountries();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(us, cn);
    }

    @Test
    void updateCountryShouldPersistChanges() {
        Country existing = new Country();
        existing.setCode("US");
        existing.setName("United States");
        
        CountryDTO dto = new CountryDTO();
        dto.setName("United States of America");
        
        when(countryRepository.findById("US")).thenReturn(Optional.of(existing));
        when(countryRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        Country updated = countryService.updateCountry("US", dto);

        assertThat(updated.getName()).isEqualTo("United States of America");
        assertThat(updated.getCode()).isEqualTo("US");
        verify(countryRepository).save(existing);
    }

    @Test
    void updateCountryShouldThrowWhenNotFound() {
        CountryDTO dto = new CountryDTO();
        dto.setName("Unknown");
        
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.updateCountry("XX", dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Country not found");
    }

    @Test
    void deleteCountryShouldRemoveCountry() {
        Country country = new Country();
        country.setCode("US");
        country.setName("United States");
        
        when(countryRepository.findById("US")).thenReturn(Optional.of(country));

        countryService.deleteCountry("US");

        verify(countryRepository).delete(country);
    }

    @Test
    void deleteCountryShouldThrowWhenNotFound() {
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.deleteCountry("XX"))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Country not found");
    }
}
