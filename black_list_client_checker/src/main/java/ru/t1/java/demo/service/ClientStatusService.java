package ru.t1.java.demo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ClientStatusService {

    public boolean isBlocked(UUID clientId, UUID accountId) {
        int hash = Math.abs(Objects.hash(clientId, accountId));
        return (hash & 1) == 0; // true для чётных хэшей
    }
}
