package com.api.demo.domain.port.output;

public interface TokenService {
    String generateToken(Long courierId, String email);
    boolean validateToken(String token);
    Long extractCourierId(String token);
    String extractEmail(String token);
}
