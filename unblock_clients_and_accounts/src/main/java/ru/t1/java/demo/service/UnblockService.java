package ru.t1.java.demo.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class UnblockService {

    private final Random random = new Random();

    public boolean tryUnblockClient(UUID clientId) {
        return random.nextBoolean();
    }

    public boolean tryUnblockAccount(UUID accountId) {
        return random.nextBoolean();
    }
}
