package com.api.pako.service.geocoding;

/**
 * A geographic point resolved from an address.
 *
 * @param latitude  WGS84 latitude in decimal degrees
 * @param longitude WGS84 longitude in decimal degrees
 */
public record Coordinates(double latitude, double longitude) {
}
