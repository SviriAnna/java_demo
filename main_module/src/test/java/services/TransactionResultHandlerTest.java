package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import ru.t1.java.demo.service.impl.TransactionResultHandler;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionResultHandlerTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private TransactionResultHandler handler;

    private UUID accountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        handler = new TransactionResultHandler(transactionRepository, accountRepository);

        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    private Transaction mockTransaction(BigDecimal amount) {
        Transaction t = new Transaction();
        t.setTransactionId(transactionId);
        t.setAmount(amount);
        t.setTransactionStatus(TransactionStatus.REQUESTED);
        return t;
    }

    private Account mockAccount(BigDecimal balance, BigDecimal frozen, AccountStatus status) {
        Account a = new Account();
        a.setAccountId(accountId);
        a.setBalance(balance);
        a.setFrozenAmount(frozen);
        a.setAccountStatus(status);
        return a;
    }

    @Test
    void testHandleAcceptedTransaction() {
        Transaction tx = mockTransaction(new BigDecimal("100.00"));
        Account acc = mockAccount(new BigDecimal("1000.00"), BigDecimal.ZERO, AccountStatus.OPEN);

        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(tx);
        when(accountRepository.findByAccountIdForUpdate(accountId)).thenReturn(Optional.of(acc));

        TransactionResultDto dto = new TransactionResultDto();
        dto.setTransactionId(transactionId);
        dto.setAccountId(accountId);
        dto.setTransactionStatus(TransactionStatus.ACCEPTED);

        handler.handle(dto);

        assertEquals(TransactionStatus.ACCEPTED, tx.getTransactionStatus());
        verify(transactionRepository).save(tx);
        verify(accountRepository, never()).save(acc);
    }

    @Test
    void testHandleBlockedTransaction() {
        Transaction tx = mockTransaction(new BigDecimal("200.00"));
        Account acc = mockAccount(new BigDecimal("1500.00"), BigDecimal.ZERO, AccountStatus.OPEN);

        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(tx);
        when(accountRepository.findByAccountIdForUpdate(accountId)).thenReturn(Optional.of(acc));

        TransactionResultDto dto = new TransactionResultDto();
        dto.setTransactionId(transactionId);
        dto.setAccountId(accountId);
        dto.setTransactionStatus(TransactionStatus.BLOCKED);

        handler.handle(dto);

        assertEquals(TransactionStatus.BLOCKED, tx.getTransactionStatus());
        assertEquals(new BigDecimal("200.00"), acc.getFrozenAmount());
        assertEquals(AccountStatus.BLOCKED, acc.getAccountStatus());

        verify(transactionRepository).save(tx);
        verify(accountRepository).save(acc);
    }

    @Test
    void testHandleRejectedTransaction() {
        Transaction tx = mockTransaction(new BigDecimal("300.00"));
        Account acc = mockAccount(new BigDecimal("500.00"), BigDecimal.ZERO, AccountStatus.OPEN);

        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(tx);
        when(accountRepository.findByAccountIdForUpdate(accountId)).thenReturn(Optional.of(acc));

        TransactionResultDto dto = new TransactionResultDto();
        dto.setTransactionId(transactionId);
        dto.setAccountId(accountId);
        dto.setTransactionStatus(TransactionStatus.REJECTED);

        handler.handle(dto);

        assertEquals(TransactionStatus.REJECTED, tx.getTransactionStatus());
        assertEquals(new BigDecimal("800.00"), acc.getBalance());

        verify(transactionRepository).save(tx);
        verify(accountRepository).save(acc);
    }

    @Test
    void testHandleTransactionNotFound() {
        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(null);

        TransactionResultDto dto = new TransactionResultDto();
        dto.setTransactionId(transactionId);
        dto.setAccountId(accountId);
        dto.setTransactionStatus(TransactionStatus.REJECTED);

        assertThrows(EntityNotFoundException.class, () -> handler.handle(dto));
        verify(accountRepository, never()).findByAccountIdForUpdate(any());
    }

    @Test
    void testHandleAccountNotFound() {
        Transaction tx = mockTransaction(new BigDecimal("100.00"));
        when(transactionRepository.findByTransactionId(transactionId)).thenReturn(tx);
        when(accountRepository.findByAccountIdForUpdate(accountId)).thenReturn(Optional.empty());

        TransactionResultDto dto = new TransactionResultDto();
        dto.setTransactionId(transactionId);
        dto.setAccountId(accountId);
        dto.setTransactionStatus(TransactionStatus.ACCEPTED);

        assertThrows(AccountNotFoundException.class, () -> handler.handle(dto));
    }
}
