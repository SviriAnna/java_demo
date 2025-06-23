package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.service.UnblockService;

import java.util.UUID;

@RestController
@RequestMapping("/api/unblock")
@RequiredArgsConstructor
public class UnblockController {

    private final UnblockService unblockService;

    @PostMapping("/client/{id}")
    public ResponseEntity<Boolean> unblockClient(@PathVariable UUID id) {
        boolean ok = unblockService.tryUnblockClient(id);
        System.out.println("Принятие решения о разблокировке клиента с id: " + id + " ответ: "+ ok);
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/account/{id}")
    public ResponseEntity<Boolean> unblockAccount(@PathVariable UUID id) {
        boolean ok = unblockService.tryUnblockAccount(id);
        System.out.println("Принятие решения о разблокировке аккаунта с id: " + id + " ответ: "+ ok);
        return ResponseEntity.ok(ok);
    }
}

