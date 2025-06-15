package ru.t1.java.demo.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionResultHandler {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void handle(TransactionResultDto dto) {
        log.info("Пришёл результат транзакции: transactionId = {}, accountId = {}, status = {}",
                dto.getTransactionId(), dto.getAccountId(), dto.getTransactionStatus());

        Transaction transaction = transactionRepository.findByTransactionId(dto.getTransactionId());
        if (transaction == null) {
            throw new EntityNotFoundException("Transaction not found with transactionId: " + dto.getTransactionId());
        }

        Account account = accountRepository.findByAccountIdForUpdate(dto.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with accountId: " + dto.getAccountId()));

        switch (dto.getTransactionStatus()) {
            case ACCEPTED -> acceptTransaction(transaction);
            case BLOCKED -> blockTransaction(transaction, account);
            case REJECTED -> rejectTransaction(transaction, account);
        }

        logAccountState(account);
    }

    private void acceptTransaction(Transaction transaction) {
        transaction.setTransactionStatus(TransactionStatus.ACCEPTED);
        transactionRepository.save(transaction);
        log.info("Транзакция подтверждена.");
    }

    private void blockTransaction(Transaction transaction, Account account) {
        transaction.setTransactionStatus(TransactionStatus.BLOCKED);
        account.setFrozenAmount(account.getFrozenAmount().add(transaction.getAmount()));
        account.setAccountStatus(AccountStatus.BLOCKED);

        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Транзакция и счёт заблокированы.");
    }

    private void rejectTransaction(Transaction transaction, Account account) {
        transaction.setTransactionStatus(TransactionStatus.REJECTED);
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().add(transaction.getAmount()));
        accountRepository.save(account);

        log.info("Транзакция отклонена, сумма возвращена на счёт.");
    }

    private void logAccountState(Account account) {
        log.info("Состояние аккаунта после обработки: баланс = {}, заморожено = {}, статус = {}",
                account.getBalance(), account.getFrozenAmount(), account.getAccountStatus());
    }
}
