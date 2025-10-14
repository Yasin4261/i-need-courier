package com.api.demo.domain.valueobject;

import java.util.Objects;

public class Phone {
    private final String number;
    
    public Phone(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        // Basic phone validation - you can enhance this
        String cleanNumber = number.replaceAll("[^0-9+]", "");
        if (cleanNumber.length() < 10) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.number = cleanNumber;
    }
    
    public String getNumber() {
        return number;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(number, phone.number);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
    
    @Override
    public String toString() {
        return number;
    }
}
