package com.api.pako.service.geocoding;

import java.util.Optional;

/**
 * Resolves a free-text address into geographic coordinates.
 *
 * <p>The single source of truth for coordinates across the system (see docs/adr/ADR-001):
 * business locations and order delivery points are derived from here rather than fabricated.
 */
public interface GeocodingService {

    /**
     * Resolve the given address to coordinates.
     *
     * @param address free-text postal address
     * @return the resolved coordinates, or {@link Optional#empty()} if the address could not
     * be resolved (no match, blank input, geocoding disabled, or an upstream failure)
     */
    Optional<Coordinates> geocode(String address);
}
