package ru.t1.java.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getById(@PathVariable UUID id) {
        TransactionDto dto = transactionService.getTransaction(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDto>> getAllByAccountId(@PathVariable UUID accountId) {
        List<TransactionDto> transactions = transactionService.getAllTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        List<TransactionDto> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(@Valid @RequestBody TransactionDto transactionDto) {
        TransactionDto created = transactionService.save(transactionDto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> update(@PathVariable UUID id, @RequestBody TransactionDto transactionDto) {
        transactionDto.setId(id);
        TransactionDto updated = transactionService.update(transactionDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
