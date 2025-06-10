package ru.t1.java.demo.cache;

import lombok.Getter;

public class CacheValue {
    @Getter
    private final Object value;
    private final long expiryTime;

    public CacheValue(Object value, long recordLifetime) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + recordLifetime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
