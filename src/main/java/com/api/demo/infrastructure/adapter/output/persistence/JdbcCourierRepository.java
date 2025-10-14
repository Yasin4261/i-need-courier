package com.api.demo.infrastructure.adapter.output.persistence;

import com.api.demo.domain.model.Courier;
import com.api.demo.domain.model.CourierStatus;
import com.api.demo.domain.port.output.CourierRepository;
import com.api.demo.domain.valueobject.Email;
import com.api.demo.domain.valueobject.Password;
import com.api.demo.domain.valueobject.Phone;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcCourierRepository implements CourierRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCourierRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Courier> courierRowMapper = (rs, rowNum) -> {
        return new Courier(
            rs.getLong("id"),
            rs.getString("name"),
            new Email(rs.getString("email")),
            new Phone(rs.getString("phone")),
            new Password(rs.getString("password_hash")),
            CourierStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("last_login_at") != null ?
                rs.getTimestamp("last_login_at").toLocalDateTime() : null
        );
    };

    @Override
    public Optional<Courier> findById(Long id) {
        String sql = """
            SELECT id, name, email, phone, password_hash, status, created_at, last_login_at 
            FROM couriers 
            WHERE id = ?
            """;

        return jdbcTemplate.query(sql, courierRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Courier> findByEmail(Email email) {
        String sql = """
            SELECT id, name, email, phone, password_hash, status, created_at, last_login_at 
            FROM couriers 
            WHERE email = ?
            """;

        try {
            return jdbcTemplate.query(sql, courierRowMapper, email.getValue())
                    .stream()
                    .findFirst();
        } catch (Exception e) {
            // Log the error and return empty
            System.err.println("Error finding courier by email: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Courier save(Courier courier) {
        if (courier.getId() == null) {
            return insert(courier);
        } else {
            return update(courier);
        }
    }

    private Courier insert(Courier courier) {
        String sql = """
            INSERT INTO couriers (name, email, phone, password_hash, status, created_at) 
            VALUES (?, ?, ?, ?, CAST(? AS user_status), ?)
            RETURNING id
            """;

        Long generatedId = jdbcTemplate.queryForObject(sql, Long.class,
            courier.getName(),
            courier.getEmail().getValue(),
            courier.getPhone().getNumber(),
            courier.getPassword().getHashedValue(),
            courier.getStatus().name(),
            Timestamp.valueOf(courier.getCreatedAt())
        );

        return new Courier(
            generatedId,
            courier.getName(),
            courier.getEmail(),
            courier.getPhone(),
            courier.getPassword(),
            courier.getStatus(),
            courier.getCreatedAt(),
            courier.getLastLoginAt()
        );
    }

    private Courier update(Courier courier) {
        String sql = """
            UPDATE couriers 
            SET name = ?, phone = ?, status = ? 
            WHERE id = ?
            """;

        jdbcTemplate.update(sql,
            courier.getName(),
            courier.getPhone().getNumber(),
            courier.getStatus().name(),
            courier.getId()
        );

        return courier;
    }

    @Override
    public boolean existsByEmail(Email email) {
        String sql = "SELECT COUNT(*) FROM couriers WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.getValue());
        return count != null && count > 0;
    }

    @Override
    public void updateLastLogin(Long courierId) {
        String sql = "UPDATE couriers SET last_login_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), courierId);
    }
}
