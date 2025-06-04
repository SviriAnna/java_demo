package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.InsufficientFundsException;
import ru.t1.java.demo.exception.TransactionNotFoundException;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public TransactionDto getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id " + id + " not found"));
        return transactionMapper.toDto(transaction);
    }

    @LogDataSourceError
    @Transactional(readOnly = true)
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

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @LogDataSourceError
    @Transactional
    @Override
    public TransactionDto save(TransactionDto transactionDto) {

        Account account = accountRepository.findById(transactionDto.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + transactionDto.getAccountId()));

        Transaction transaction = transactionMapper.toEntity(transactionDto);
        transaction.setId(null);
        adjustAccountBalance(account, transaction.getAmount().negate());
        accountRepository.save(account);

        transaction.setAccount(account);
        transaction.setTransactionTime(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toDto(saved);
    }

    @LogDataSourceError
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

    @LogDataSourceError
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

    /**
     * Универсальный метод изменения баланса.
     *
     * @param account счёт
     * @param delta   сумма изменения (отрицательное значение — списание)
     */
    private void adjustAccountBalance(Account account, BigDecimal delta) {
        BigDecimal newBalance = account.getBalance().add(delta);

        if (account.getAccountType() == AccountType.DEBIT && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(account.getId(), account.getBalance(), delta);
        }

        account.setBalance(newBalance);
    }
}
