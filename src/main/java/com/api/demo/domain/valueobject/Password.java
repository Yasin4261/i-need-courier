package com.api.demo.domain.valueobject;

import com.api.demo.domain.exception.InvalidPasswordException;

import java.util.Objects;

public class Password {
    private final String hashedValue;

    public Password(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new InvalidPasswordException("Password hash cannot be null or empty");
        }
        this.hashedValue = hashedValue;
    }

    public String getHashedValue() {
        return hashedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
}
