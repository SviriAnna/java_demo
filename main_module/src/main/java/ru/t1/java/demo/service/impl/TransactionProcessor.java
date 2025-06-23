package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.dto.MessageResponse;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.kafka.TransactionAcceptMessage;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.JwtTokenProvider;
import ru.t1.java.demo.web.Service2Client;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessor {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final KafkaTransactionProducer kafkaTransactionProducer;
    private final Service2Client service2Client;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${transaction.max-rejected-count}")
    private int maxRejectedCount;

    @Transactional
    public void process(TransactionDto dto) {
        log.info("Началась проверка поступившей транзакции");
        Account account = accountRepository.findByIdForUpdate(dto.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + dto.getAccountId()));
        Client client = account.getClient();

        if (client.getClientStatus() == null) {
            if (checkAndBlockClient(dto, account, client)) return;
        }

        Boolean bol = checkRejectedLimit(dto, account);
        if (client.getClientStatus() != null && bol) return;
        log.info("Bol is {}", bol);

        performTransaction(dto, account);
    }

    private boolean checkAndBlockClient(TransactionDto dto, Account account, Client client) {
            String token = jwtTokenProvider.generateToken(client.getClientId().toString());
            MessageResponse messageResponse = service2Client.checkClientStatusWithToken(client.getClientId(), account.getAccountId(), token).block();
            log.info("Сервис проверки клиентов вернул статус isBlocked: {}",messageResponse.getIsBlocked());
            assert messageResponse != null;
            if (Boolean.TRUE.equals(messageResponse.getIsBlocked())) {
                account.setAccountStatus(AccountStatus.BLOCKED);
                client.setClientStatus(ClientStatus.BLOCKED);
                accountRepository.save(account);
                clientRepository.save(client);
                saveRejected(dto, account);
                log.info("Клиент и его счет были заблокированы.");
                return true;
            } else if (Boolean.FALSE.equals(messageResponse.getIsBlocked())) {
                client.setClientStatus(ClientStatus.ACTIVE);
                clientRepository.save(client);
            }
        return false;
    }

    private boolean checkRejectedLimit(TransactionDto dto, Account account) {
        long count = transactionRepository.countByAccountIdAndTransactionStatus(dto.getAccountId(), TransactionStatus.REJECTED);
        if (count+1 >= maxRejectedCount) {
            account.setAccountStatus(AccountStatus.ARRESTED);
            accountRepository.save(account);
            saveRejected(dto, account);
            log.info("Аккунту выставлен статус ARRESTED.");
            return true;
        }
        return false;
    }

    private void performTransaction(TransactionDto dto, Account account) {
        Transaction t = transactionMapper.toEntity(dto);
//        t.setId(null); t.setTransactionId(UUID.randomUUID());
        t.setId(dto.getId());
        t.setTransactionId(dto.getTransactionId());
        t.setAccount(account);
        t.setTransactionTime(LocalDateTime.now());

        if (!account.getAccountStatus().equals(AccountStatus.OPEN)) {
            t.setTransactionStatus(TransactionStatus.REJECTED);
            transactionRepository.save(t);
            log.warn("Транзакция отклонена, так как счет клиента не OPEN");
            return;
        }

        t.setTransactionStatus(TransactionStatus.REQUESTED);
        account.setBalance(account.getBalance().subtract(dto.getAmount()));
        accountRepository.save(account);
        Transaction saved = transactionRepository.save(t);

        kafkaTransactionProducer.sendTransactionAccepted(new TransactionAcceptMessage(
            account.getClient().getClientId(),
            account.getAccountId(),
            saved.getTransactionId(),
            saved.getTransactionTime(),
            saved.getAmount(),
            account.getBalance().add(saved.getAmount())
        ));
        log.info("Транзакция со статусом REQUESTED была отправлена после проверки на статус счета OPEN.");
    }

    private void saveRejected(TransactionDto dto, Account account) {
        Transaction t = transactionMapper.toEntity(dto);
//        t.setId(null); t.setTransactionId(UUID.randomUUID());
        t.setId(dto.getId());
        t.setTransactionId(dto.getTransactionId());
        t.setAccount(account);
        t.setTransactionTime(LocalDateTime.now());
        t.setTransactionStatus(TransactionStatus.REJECTED);
        transactionRepository.save(t);
    }
}
