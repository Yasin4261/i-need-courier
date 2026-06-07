package com.api.pako.service.geocoding;

import com.api.pako.config.GeocodingProperties;
import com.api.pako.model.Business;
import com.api.pako.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCoordinateBackfillRunnerTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private GeocodingService geocodingService;

    private GeocodingProperties properties;
    private BusinessCoordinateBackfillRunner underTest;

    @BeforeEach
    void setUp() {
        properties = new GeocodingProperties();
        properties.getBackfill().setEnabled(true);
        underTest = new BusinessCoordinateBackfillRunner(properties, businessRepository, geocodingService);
    }

    @Test
    void backfillsCoordinatesForGeocodableBusiness() {
        // GIVEN
        var business = businessWithAddress(1L, "Bagdat Caddesi 100, Istanbul");
        when(businessRepository.findByLatitudeIsNullOrLongitudeIsNull()).thenReturn(List.of(business));
        when(geocodingService.geocode("Bagdat Caddesi 100, Istanbul"))
                .thenReturn(Optional.of(new Coordinates(40.9906, 29.0246)));

        // WHEN
        underTest.run(null);

        // THEN
        verify(businessRepository).save(assertArg(saved -> {
            assertThat(saved.getLatitude()).isEqualTo(40.9906);
            assertThat(saved.getLongitude()).isEqualTo(29.0246);
        }));
    }

    @Test
    void leavesBusinessUntouchedWhenGeocodingFails() {
        // GIVEN
        var business = businessWithAddress(2L, "unresolvable address");
        when(businessRepository.findByLatitudeIsNullOrLongitudeIsNull()).thenReturn(List.of(business));
        when(geocodingService.geocode("unresolvable address")).thenReturn(Optional.empty());

        // WHEN
        underTest.run(null);

        // THEN - nothing saved, coordinates remain null for a retry on the next startup
        verify(businessRepository, never()).save(any());
        assertThat(business.getLatitude()).isNull();
        assertThat(business.getLongitude()).isNull();
    }

    @Test
    void savesOnlyTheBusinessesThatCouldBeGeocoded() {
        // GIVEN
        var resolvable = businessWithAddress(1L, "good address");
        var unresolvable = businessWithAddress(2L, "bad address");
        when(businessRepository.findByLatitudeIsNullOrLongitudeIsNull())
                .thenReturn(List.of(resolvable, unresolvable));
        when(geocodingService.geocode("good address")).thenReturn(Optional.of(new Coordinates(41.0, 29.0)));
        when(geocodingService.geocode("bad address")).thenReturn(Optional.empty());

        // WHEN
        underTest.run(null);

        // THEN - exactly one save, for the resolvable business
        verify(businessRepository).save(assertArg(saved -> assertThat(saved.getId()).isEqualTo(1L)));
    }

    @Test
    void doesNothingWhenNoBusinessesNeedCoordinates() {
        // GIVEN
        when(businessRepository.findByLatitudeIsNullOrLongitudeIsNull()).thenReturn(List.of());

        // WHEN
        underTest.run(null);

        // THEN
        verifyNoInteractions(geocodingService);
        verify(businessRepository, never()).save(any());
    }

    @Test
    void skipsEntirelyWhenBackfillDisabled() {
        // GIVEN
        properties.getBackfill().setEnabled(false);

        // WHEN
        underTest.run(null);

        // THEN - not even the lookup query runs
        verifyNoInteractions(businessRepository, geocodingService);
    }

    private static Business businessWithAddress(Long id, String address) {
        var business = new Business();
        business.setId(id);
        business.setName("Business " + id);
        business.setAddress(address);
        return business;
    }
}
