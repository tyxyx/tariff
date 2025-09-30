package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.repository.TariffRepository;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void addTariffShouldPersistEntity() {
        Tariff tariff = buildTariff();
        when(tariffRepository.save(tariff)).thenReturn(tariff);

        Tariff saved = tariffService.addTariff(tariff);

        assertThat(saved).isSameAs(tariff);
        verify(tariffRepository).save(tariff);
    }

    @Test
    void updateTariffShouldApplyIncomingValues() {
        UUID id = UUID.randomUUID();
        Tariff existing = buildTariff();
        existing.setId(id);

        Tariff incoming = new Tariff();
        incoming.setProductName("Updated Product");
        incoming.setHTSCode("9999.00");
        incoming.setOriginCountry("SG");
        incoming.setDestCountry("US");
        incoming.setTariffRate(12.5);
        incoming.setTariffEffectiveDate(LocalDate.of(2024, 1, 10));
        incoming.setTariffExpiryDate(LocalDate.of(2024, 12, 31));

        when(tariffRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tariffRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        Tariff updated = tariffService.updateTariff(id, incoming);

        assertThat(updated.getProductName()).isEqualTo("Updated Product");
        assertThat(updated.getHTSCode()).isEqualTo("9999.00");
        assertThat(updated.getOriginCountry()).isEqualTo("SG");
        assertThat(updated.getDestCountry()).isEqualTo("US");
        assertThat(updated.getTariffRate()).isEqualTo(12.5);
        assertThat(updated.getTariffEffectiveDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(updated.getTariffExpiryDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        verify(tariffRepository).save(existing);
    }

    @Test
    void updateTariffShouldThrowWhenTariffMissing() {
        UUID id = UUID.randomUUID();
        Tariff request = buildTariff();
        when(tariffRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tariffService.updateTariff(id, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Tariff not found");
    }

    @Test
    void getParticularTariffShouldReturnMatch() {
        ParticularTariffDTO dto = new ParticularTariffDTO();
        dto.setProductName("Widgets");
        dto.setOriginCountry("SG");
        dto.setDestCountry("US");
        dto.setDate(LocalDate.of(2024, 5, 1));

        Tariff tariff = buildTariff();
        when(tariffRepository.getTariffFromProductCountriesAndDates(
            dto.getProductName(), dto.getDate(), dto.getOriginCountry(), dto.getDestCountry()))
            .thenReturn(Optional.of(tariff));

        Tariff result = tariffService.getParticularTariff(dto);

        assertThat(result).isSameAs(tariff);
    }

    @Test
    void getParticularTariffShouldThrowWhenMissing() {
        ParticularTariffDTO dto = new ParticularTariffDTO();
        dto.setProductName("Widgets");
        dto.setOriginCountry("SG");
        dto.setDestCountry("US");
        dto.setDate(LocalDate.of(2024, 5, 1));

        when(tariffRepository.getTariffFromProductCountriesAndDates(
            dto.getProductName(), dto.getDate(), dto.getOriginCountry(), dto.getDestCountry()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tariffService.getParticularTariff(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("No tariff found");
    }

    private Tariff buildTariff() {
        Tariff tariff = new Tariff();
        tariff.setProductName("Widgets");
        tariff.setHTSCode("1234.56");
        tariff.setOriginCountry("CN");
        tariff.setDestCountry("US");
        tariff.setTariffRate(5.0);
        tariff.setTariffEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setTariffExpiryDate(LocalDate.of(2024, 6, 30));
        return tariff;
    }
}
