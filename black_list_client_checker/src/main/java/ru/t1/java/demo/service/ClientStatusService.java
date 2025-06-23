package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class ClientStatusService {

    public boolean isBlocked(UUID clientId, UUID accountId) {
        log.info("isBlocked called with clientId: {}, accountId: {}", clientId, accountId);
        int hash = Math.abs(Objects.hash(clientId, accountId));
        return (hash & 1) == 0; // true для чётных хэшей
    }
}
