package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.TransactionDto;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionDto getTransaction(UUID id);

    List<TransactionDto> getAllTransactionsByAccountId(UUID accountId);

    TransactionDto save(TransactionDto transactionDto);

    TransactionDto update(TransactionDto transactionDto);

    void deleteById(UUID id);

    List<TransactionDto> getAllTransactions();
}
