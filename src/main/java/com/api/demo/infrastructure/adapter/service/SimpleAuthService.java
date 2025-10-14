package com.api.demo.infrastructure.adapter.service;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;
import com.api.demo.application.dto.CourierRegistrationRequest;
import com.api.demo.application.dto.CourierRegistrationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class SimpleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAuthService.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;

    public SimpleAuthService(JdbcTemplate jdbcTemplate,
                           PasswordEncoder passwordEncoder,
                           JwtTokenGenerator jwtTokenGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    public CourierRegistrationResponse register(CourierRegistrationRequest request) {
        try {
            // 1. Email kontrolü - basit
            String checkEmailSql = "SELECT COUNT(*) FROM couriers WHERE email = ?";
            Integer emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, request.getEmail());

            if (emailCount != null && emailCount > 0) {
                throw new RuntimeException("COURIER_ALREADY_EXISTS:Email already exists: " + request.getEmail());
            }

            // 2. Telefon kontrolü - yeni eklendi
            String checkPhoneSql = "SELECT COUNT(*) FROM couriers WHERE phone = ?";
            Integer phoneCount = jdbcTemplate.queryForObject(checkPhoneSql, Integer.class, request.getPhone());

            if (phoneCount != null && phoneCount > 0) {
                throw new RuntimeException("PHONE_ALREADY_EXISTS:Phone number already exists: " + request.getPhone());
            }

            // 3. Password hash'le - basit
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            // 4. Insert - basit
            String insertSql = """
                INSERT INTO couriers (name, email, phone, password_hash, status, created_at) 
                VALUES (?, ?, ?, ?, CAST('INACTIVE' AS user_status), ?) 
                RETURNING id
                """;

            Long courierId = jdbcTemplate.queryForObject(insertSql, Long.class,
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                hashedPassword,
                Timestamp.valueOf(LocalDateTime.now())
            );

            logger.info("Courier registered successfully with ID: {} for email: {}", courierId, request.getEmail());

            return new CourierRegistrationResponse(
                courierId,
                request.getName(),
                request.getEmail(),
                "Registration successful"
            );

        } catch (Exception e) {
            logger.error("Registration failed for email: {} - Error: {}", request.getEmail(), e.getMessage(), e);

            // Spesifik hata mesajları
            if (e.getMessage().contains("COURIER_ALREADY_EXISTS")) {
                throw new RuntimeException("Courier already exists with email: " + request.getEmail());
            }
            if (e.getMessage().contains("PHONE_ALREADY_EXISTS")) {
                throw new RuntimeException("Phone number already in use: " + request.getPhone());
            }
            if (e.getMessage().contains("phone") && e.getMessage().contains("unique")) {
                throw new RuntimeException("Phone number already in use: " + request.getPhone());
            }
            if (e.getMessage().contains("email") && e.getMessage().contains("unique")) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }

            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public CourierLoginResponse login(CourierLoginRequest request) {
        try {
            // 1. Kullanıcıyı bul - veritabanı yapısına uygun
            String findUserSql = "SELECT id, name, email, password_hash FROM couriers WHERE email = ?";

            var courier = jdbcTemplate.queryForObject(findUserSql, (rs, rowNum) -> {
                return new CourierData(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash")
                );
            }, request.getEmail());

            // 2. Null check - PostgreSQL queryForObject null dönebilir
            if (courier == null) {
                throw new RuntimeException("User not found");
            }

            // 3. Password kontrol et - basit
            if (!passwordEncoder.matches(request.getPassword(), courier.passwordHash())) {
                throw new RuntimeException("Invalid password");
            }

            // 4. JWT token oluştur - basit
            String token = jwtTokenGenerator.generateToken(courier.id(), courier.email());

            // 5. Opsiyonel: updated_at güncelle (last_login_at yerine)
            try {
                String updateSql = "UPDATE couriers SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                jdbcTemplate.update(updateSql, courier.id());
            } catch (Exception updateException) {
                // Update hatası login'i etkilemesin, sadece logla
                logger.warn("Failed to update login time for courier {}: {}", courier.id(), updateException.getMessage());
            }

            logger.info("Login successful for courier ID: {}", courier.id());

            return new CourierLoginResponse(
                token,
                courier.id(),
                courier.name(),
                courier.email(),
                "Login successful"
            );

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Kullanıcı bulunamadı
            logger.error("User not found: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        } catch (Exception e) {
            logger.error("Login failed for email: {}", request.getEmail(), e);
            throw new RuntimeException("Invalid email or password");
        }
    }

    // Basit data class
    private record CourierData(Long id, String name, String email, String passwordHash) {}
}
