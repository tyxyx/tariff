package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.backend.dto.AddTariffDTO;
import com.tariff.backend.dto.ParticularTariffDTO;
import com.tariff.backend.dto.ProductDTO;
import com.tariff.backend.exception.BadRequestException;
import com.tariff.backend.exception.NotFoundException;
import com.tariff.backend.model.Product;
import com.tariff.backend.model.Tariff;
import com.tariff.backend.repository.ProductRepository;
import com.tariff.backend.repository.TariffRepository;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void addTariffShouldPersistEntity() {
        AddTariffDTO request = buildAddTariffDTO();
        when(tariffRepository.getTariffsByHtsCode(request.getHtscode())).thenReturn(List.of());
        when(tariffRepository.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tariff saved = tariffService.addTariff(request);

        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        verify(tariffRepository, times(1)).save(captor.capture());
        Tariff persisted = captor.getValue();

        assertThat(persisted.getHTSCode()).isEqualTo(request.getHtscode());
        assertThat(persisted.getOriginCountry()).isEqualTo(request.getOriginCountry());
        assertThat(persisted.getDestCountry()).isEqualTo(request.getDestCountry());
        assertThat(persisted.getEffectiveDate()).isEqualTo(request.getEffectiveDate());
        assertThat(persisted.getExpiryDate()).isEqualTo(request.getExpiryDate());
        assertThat(persisted.getRate()).isEqualTo(request.getRate());
        assertThat(persisted.isEnabled()).isEqualTo(request.isEnabled());
        assertThat(persisted.getProducts()).hasSize(1);
        Product product = persisted.getProducts().get(0);
        assertThat(product.getName()).isEqualTo(request.getProducts().get(0).getName());
        assertThat(product.getDescription()).isEqualTo(request.getProducts().get(0).getDescription());
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void addTariffShouldUpdatePreviousExpiryWhenPeriodsDoNotOverlap() {
        AddTariffDTO request = buildAddTariffDTO();
        request.setEffectiveDate(LocalDate.of(2024, 7, 1));
        request.setExpiryDate(LocalDate.of(2024, 12, 31));

        Tariff existing = new Tariff();
        existing.setHTSCode(request.getHtscode());
        existing.setOriginCountry(request.getOriginCountry());
        existing.setDestCountry(request.getDestCountry());
        existing.setExpiryDate(LocalDate.of(2024, 6, 30));
        when(tariffRepository.getTariffsByHtsCode(request.getHtscode())).thenReturn(List.of(existing));
        when(tariffRepository.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tariffService.addTariff(request);

        assertThat(existing.getExpiryDate()).isEqualTo(request.getEffectiveDate().minusDays(1));
        verify(tariffRepository).save(existing);
    }

    @Test
    void addTariffShouldThrowWhenEffectiveAfterExpiry() {
        AddTariffDTO request = buildAddTariffDTO();
        request.setEffectiveDate(LocalDate.of(2024, 8, 1));
        request.setExpiryDate(LocalDate.of(2024, 7, 31));

        assertThatThrownBy(() -> tariffService.addTariff(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Effective date cannot be after expiry date");
    }

    @Test
    void updateTariffShouldApplyIncomingValues() {
        UUID id = UUID.randomUUID();
        Tariff existing = buildTariff();
        existing.setId(id);

        Tariff incoming = new Tariff();
        incoming.setHTSCode("9999.00");
        incoming.setOriginCountry("SG");
        incoming.setDestCountry("US");
        incoming.setRate(12.5);
        incoming.setEffectiveDate(LocalDate.of(2024, 1, 10));
        incoming.setExpiryDate(LocalDate.of(2024, 12, 31));

        when(tariffRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tariffRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        Tariff updated = tariffService.updateTariff(id, incoming);

        assertThat(updated.getHTSCode()).isEqualTo("9999.00");
        assertThat(updated.getOriginCountry()).isEqualTo("SG");
        assertThat(updated.getDestCountry()).isEqualTo("US");
        assertThat(updated.getRate()).isEqualTo(12.5);
        assertThat(updated.getEffectiveDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(updated.getExpiryDate()).isEqualTo(LocalDate.of(2024, 12, 31));
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

    private AddTariffDTO buildAddTariffDTO() {
        AddTariffDTO dto = new AddTariffDTO();
        dto.setOriginCountry("CN");
        dto.setDestCountry("US");
        dto.setEffectiveDate(LocalDate.of(2024, 1, 1));
        dto.setExpiryDate(LocalDate.of(2024, 6, 30));
        dto.setRate(5.0);
        dto.setEnabled(true);
        dto.setHtscode("1234.56");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Widgets");
        productDTO.setDescription("Widget Description");
        productDTO.setEnabled(true);
        dto.setProducts(List.of(productDTO));
        return dto;
    }

    private Tariff buildTariff() {
        Tariff tariff = new Tariff();
        tariff.setHTSCode("1234.56");
        tariff.setOriginCountry("CN");
        tariff.setDestCountry("US");
        tariff.setRate(5.0);
        tariff.setEffectiveDate(LocalDate.of(2024, 1, 1));
        tariff.setExpiryDate(LocalDate.of(2024, 6, 30));
        tariff.setEnabled(true);
        return tariff;
    }
}
