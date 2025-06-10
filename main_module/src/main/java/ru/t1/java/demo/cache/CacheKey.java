package ru.t1.java.demo.cache;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class CacheKey {
    private final Method method;
    private final Object[] args;

    public CacheKey(Method method, Object[] args) {
        this.method = method;
        this.args = args != null ? args.clone() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey that)) return false;
        return Objects.equals(method, that.method) &&
                Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, Arrays.deepHashCode(args));
    }
}
