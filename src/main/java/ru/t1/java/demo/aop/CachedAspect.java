package ru.t1.java.demo.aop;

import ru.t1.java.demo.cache.CacheKey;
import ru.t1.java.demo.cache.CacheProperties;
import ru.t1.java.demo.cache.CacheValue;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class CachedAspect {

    private final CacheProperties cacheProperties;
    private final Map<CacheKey, CacheValue> cache = new ConcurrentHashMap<>();

    @Around("@annotation(ru.t1.java.demo.aop.annotation.Cached)")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        cleanUpExpiredEntries();

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        CacheKey key = new CacheKey(method, args);
        CacheValue cached = cache.get(key);

        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }

        Object result = joinPoint.proceed();

        cache.put(key, new CacheValue(result, cacheProperties.getRecordLifetime()));
        return result;
    }

    // Метод для очистки устаревших записей
    private void cleanUpExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
