package ru.t1.java.demo.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.annotation.Cached;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.aop.annotation.Metric;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.TransactionNotFoundException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.kafka.TransactionAcceptMessage;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
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
    private final KafkaTransactionProducer kafkaTransactionProducer;

    @Cached
    @Metric
    @LogDataSourceError
    @Override
    public TransactionDto getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id " + id + " not found"));
        return transactionMapper.toDto(transaction);
    }

    @Cached
    @Metric
    @LogDataSourceError
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
    @Metric
    @LogDataSourceError
    @Override
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Metric
    @LogDataSourceError
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

    @Metric
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

    @Metric
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

    @Override
    @Transactional
    public void processTransaction(TransactionDto transactionDto) {
        log.info("Началась обработка транзакции: {}", transactionDto);

        try {
            log.info("Поиск счёта с ID: {}", transactionDto.getAccountId());
            Account account = accountRepository.findByIdForUpdate(transactionDto.getAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + transactionDto.getAccountId()));
            log.info("Счёт найден: ID={}, статус={}, баланс={}", account.getAccountId(), account.getAccountStatus(), account.getBalance());

            Transaction transaction = transactionMapper.toEntity(transactionDto);
            transaction.setId(null);
            transaction.setTransactionId(UUID.randomUUID());
            transaction.setAccount(account);
            transaction.setTransactionTime(LocalDateTime.now());

            if (!account.getAccountStatus().equals(AccountStatus.OPEN)) {
                log.warn("Попытка провести транзакцию по счёту с недопустимым статусом: {}", account.getAccountStatus());
                transaction.setTransactionStatus(TransactionStatus.REJECTED);
                transactionRepository.save(transaction);
                log.info("Транзакция сохранена с статусом REJECTED: transactionId={}", transaction.getTransactionId());
                return;
            }

            transaction.setTransactionStatus(TransactionStatus.REQUESTED);

            log.info("Корректировка баланса на сумму: {}", transaction.getAmount().negate());
            adjustAccountBalance(account, transaction.getAmount().negate());
            accountRepository.save(account);
            log.info("Счёт обновлён: ID={}, новый баланс={}", account.getAccountId(), account.getBalance());

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Транзакция сохранена: ID={}, transactionId={}", savedTransaction.getId(), savedTransaction.getTransactionId());

            TransactionAcceptMessage acceptMessage = new TransactionAcceptMessage(
                    account.getClient().getClientId(),
                    account.getAccountId(),
                    savedTransaction.getTransactionId(),
                    savedTransaction.getTransactionTime(),
                    savedTransaction.getAmount(),
                    account.getBalance().add(savedTransaction.getAmount())
            );
            kafkaTransactionProducer.sendTransactionAccepted(acceptMessage);
            log.info("Отправлено сообщение о приёме транзакции со статусом REQUESTED в Kafka: {}", acceptMessage);

        } catch (Exception e) {
            log.error("Ошибка при обработке транзакции: {}", transactionDto, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void handleTransactionResult(TransactionResultDto dto) {
        log.info("Из t1_demo_transaction_result пришла транзакция с параметрами: transactionId = {}, accountId = {}, transactionStatus = {}",
                dto.getTransactionId(), dto.getAccountId(), dto.getTransactionStatus());

        Transaction transaction = transactionRepository.findByTransactionId(dto.getTransactionId());
        if (transaction == null) {
            throw new EntityNotFoundException("Transaction not found: " + dto.getTransactionId());
        }

        log.info("Производится поиск аккаунта по транзакции");
        Account account = accountRepository.findByAccountIdForUpdate(dto.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + dto.getAccountId()));

        log.info("По данной транзакции найден аккаунт со статусом {}", account.getAccountStatus());

        switch (dto.getTransactionStatus()) {
            case ACCEPTED -> {
                log.info("Производится подтверждение транзакции");
                transaction.setTransactionStatus(TransactionStatus.ACCEPTED);
                transactionRepository.save(transaction);
            }
            case BLOCKED -> {
                log.info("Производится блокировка транзакции");
                transaction.setTransactionStatus(TransactionStatus.BLOCKED);

                log.info("Производится заморозка на сумму транзакции: {}", transaction.getAmount());
                account.setFrozenAmount(account.getFrozenAmount().add(transaction.getAmount()));

                log.info("Производится блокировка счета");
                account.setAccountStatus(AccountStatus.BLOCKED);

                accountRepository.save(account);
            }
            case REJECTED -> {
                log.info("Производится отклонение транзакции");
                transaction.setTransactionStatus(TransactionStatus.REJECTED);
                transactionRepository.save(transaction);
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                accountRepository.save(account);
            }
        }
        log.info("Транзакции присовен статус {}, произведено сохранение в базу", transaction.getTransactionStatus());
        log.info("Аккаунту присвоены следующие параметры: сумма заморозки {}, баланс {}, статус {}", account.getFrozenAmount(), account.getBalance(), account.getAccountStatus());
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
