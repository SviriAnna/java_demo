package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aspect.annotation.Cached;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.TransactionNotFoundException;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionProcessor processor;
    private final TransactionResultHandler transactionResultHandler;

    @Value("${transaction.max-rejected-count}")
    private int maxRejectedCount;

    @Cached
    @Override
    public TransactionDto getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id " + id + " not found"));
        return transactionMapper.toDto(transaction);
    }

    @Cached
    @Override
    public List<TransactionDto> getAllTransactionsByAccountId(UUID accountId) {
        boolean accountExists = accountRepository.existsById(accountId);
        if (!accountExists) {
            throw new AccountNotFoundException("Transactions with accountID " + accountId + " not found.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cached
    @Override
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TransactionDto save(TransactionDto transactionDto) {

        Account account = accountRepository.findById(transactionDto.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + transactionDto.getAccountId()));

        Transaction transaction = transactionMapper.toEntity(transactionDto);
        transaction.setId(null);
        transaction.setTransactionId(UUID.randomUUID());
        adjustAccountBalance(account, transaction.getAmount().negate());
        accountRepository.save(account);

        transaction.setAccount(account);
        transaction.setTransactionTime(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toDto(saved);
    }

    @Transactional
    @Override
    public TransactionDto update(TransactionDto transactionDto) {
        if (transactionDto.getId() == null) {
            throw new IllegalArgumentException("Transaction ID must not be null for update");
        }

        Transaction existing = transactionRepository.findById(transactionDto.getId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id " + transactionDto.getId() + " not found"));

        Account account = existing.getAccount();
        BigDecimal oldAmount = existing.getAmount();
        BigDecimal newAmount = transactionDto.getAmount();

        adjustAccountBalance(account, oldAmount);
        adjustAccountBalance(account, newAmount.negate());
        accountRepository.save(account);

        transactionMapper.updateEntityFromDto(transactionDto, existing);
        Transaction updated = transactionRepository.save(existing);

        return transactionMapper.toDto(updated);
    }

    @Transactional
    @Override
    public void deleteById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id " + id + " not found"));

        Account account = transaction.getAccount();
        adjustAccountBalance(account, transaction.getAmount());
        accountRepository.save(account);

        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void processTransaction(TransactionDto dto) {
        try {
            processor.process(dto);
        } catch (Exception e) {
            log.error("Ошибка при обработке транзакции: {}", dto, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void handleTransactionResult(TransactionResultDto dto) {
        transactionResultHandler.handle(dto);
    }

    /**
     * Универсальный метод изменения баланса.
     *
     * @param account счёт
     * @param delta   сумма изменения (отрицательное значение — списание)
     */
    private void adjustAccountBalance(Account account, BigDecimal delta) {
        BigDecimal newBalance = account.getBalance().add(delta);

        account.setBalance(newBalance);
    }
}