package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.TransactionNotFoundException;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.impl.TransactionProcessor;
import ru.t1.java.demo.service.impl.TransactionResultHandler;
import ru.t1.java.demo.service.impl.TransactionServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionProcessor processor;

    @Mock
    private TransactionResultHandler resultHandler;

    private UUID transactionId;
    private UUID accountId;
    private Transaction transaction;
    private TransactionDto transactionDto;
    private Account account;

    @BeforeEach
    void init() {
        transactionId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accountId);
        account.setBalance(BigDecimal.valueOf(1000));

        transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setAccount(account);
        transaction.setTransactionTime(LocalDateTime.now());

        transactionDto = new TransactionDto();
        transactionDto.setId(transactionId);
        transactionDto.setTransactionId(transaction.getTransactionId());
        transactionDto.setAccountId(accountId);
        transactionDto.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    void getTransaction_found_success() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        TransactionDto result = transactionService.getTransaction(transactionId);

        assertEquals(transactionDto, result);
    }

    @Test
    void getTransaction_notFound_throwsException() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(transactionId));
    }

    @Test
    void getAllTransactionsByAccountId_found_success() {
        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        List<TransactionDto> result = transactionService.getAllTransactionsByAccountId(accountId);

        assertEquals(1, result.size());
        assertEquals(transactionDto, result.get(0));
    }

    @Test
    void getAllTransactionsByAccountId_notFound_throwsException() {
        when(accountRepository.existsById(accountId)).thenReturn(false);
        assertThrows(AccountNotFoundException.class,
                     () -> transactionService.getAllTransactionsByAccountId(accountId));
    }

    @Test
    void getAllTransactions_returnsList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        List<TransactionDto> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
    }

    @Test
    void save_success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionMapper.toEntity(transactionDto)).thenReturn(transaction);
        when(transactionRepository.save(any())).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        TransactionDto result = transactionService.save(transactionDto);

        assertNotNull(result);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void save_accountNotFound_throwsException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.save(transactionDto));
    }

    @Test
    void update_success() {
        transactionDto.setAmount(BigDecimal.valueOf(150));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionMapper).updateEntityFromDto(transactionDto, transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        TransactionDto result = transactionService.update(transactionDto);

        assertEquals(transactionDto, result);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void update_nullId_throwsException() {
        transactionDto.setId(null);
        assertThrows(IllegalArgumentException.class, () -> transactionService.update(transactionDto));
    }

    @Test
    void update_transactionNotFound_throwsException() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.update(transactionDto));
    }

    @Test
    void deleteById_success() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        transactionService.deleteById(transactionId);

        verify(accountRepository).save(account);
        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void deleteById_transactionNotFound_throwsException() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteById(transactionId));
    }

    @Test
    void processTransaction_success() {
        doNothing().when(processor).process(transactionDto);
        assertDoesNotThrow(() -> transactionService.processTransaction(transactionDto));
    }

    @Test
    void handleTransactionResult_success() {
        TransactionResultDto resultDto = new TransactionResultDto();
        doNothing().when(resultHandler).handle(resultDto);
        assertDoesNotThrow(() -> transactionService.handleTransactionResult(resultDto));
    }
}
