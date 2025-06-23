package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
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
import ru.t1.java.demo.service.impl.TransactionProcessor;
import ru.t1.java.demo.util.JwtTokenProvider;
import ru.t1.java.demo.web.Service2Client;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionProcessorTest {

    @InjectMocks
    private TransactionProcessor transactionProcessor;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private KafkaTransactionProducer kafkaTransactionProducer;
    @Mock
    private Service2Client service2Client;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionProcessor = new TransactionProcessor(
                accountRepository,
                clientRepository,
                transactionRepository,
                transactionMapper,
                kafkaTransactionProducer,
                service2Client,
                jwtTokenProvider
        );
        // Хардкод значения из application.yml
        ReflectionTestUtils.setField(transactionProcessor, "maxRejectedCount", 3);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        UUID accId = UUID.randomUUID();
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(accId);

        when(accountRepository.findByIdForUpdate(accId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionProcessor.process(dto));
    }

    @Test
    void shouldBlockClientIfExternalServiceSaysBlocked() {
        // Setup
        UUID accId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(accId);

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accId);
        account.setAccountStatus(AccountStatus.OPEN);
        Client client = new Client();
        client.setClientId(clientId);
        account.setClient(client);

        when(accountRepository.findByIdForUpdate(accId)).thenReturn(Optional.of(account));
        when(jwtTokenProvider.generateToken(clientId.toString())).thenReturn("mock-token");
        when(service2Client.checkClientStatusWithToken(eq(clientId), eq(accId), any()))
                .thenReturn(Mono.just(new MessageResponse(true)));

        Transaction mockTx = new Transaction();
        when(transactionMapper.toEntity(any())).thenReturn(mockTx);

        transactionProcessor.process(dto);

        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());
        assertEquals(ClientStatus.BLOCKED, client.getClientStatus());
        verify(accountRepository).save(account);
        verify(clientRepository).save(client);
        verify(transactionRepository).save(mockTx);
    }

    @Test
    void shouldArrestAccountWhenRejectedLimitExceeded() {
        // Setup
        UUID accId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(accId);

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accId);
        account.setAccountStatus(AccountStatus.OPEN);
        Client client = new Client();
        client.setClientId(clientId);
        client.setClientStatus(ClientStatus.ACTIVE);
        account.setClient(client);

        when(accountRepository.findByIdForUpdate(accId)).thenReturn(Optional.of(account));
        when(transactionRepository.countByAccountIdAndTransactionStatus(accId, TransactionStatus.REJECTED)).thenReturn(2); // уже 2
        Transaction mockTx = new Transaction();
        when(transactionMapper.toEntity(any())).thenReturn(mockTx);

        transactionProcessor.process(dto);

        assertEquals(AccountStatus.ARRESTED, account.getAccountStatus());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(mockTx);
    }

    @Test
    void shouldRejectTransactionIfAccountNotOpen() {
        UUID accId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        TransactionDto dto = new TransactionDto();
        dto.setAccountId(accId);

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accId);
        account.setAccountStatus(AccountStatus.CLOSED);
        Client client = new Client();
        client.setClientId(clientId);
        client.setClientStatus(ClientStatus.ACTIVE);
        account.setClient(client);

        Transaction transaction = new Transaction();
        when(accountRepository.findByIdForUpdate(accId)).thenReturn(Optional.of(account));
        when(transactionRepository.countByAccountIdAndTransactionStatus(accId, TransactionStatus.REJECTED)).thenReturn(0);
        when(transactionMapper.toEntity(dto)).thenReturn(transaction);

        transactionProcessor.process(dto);

        assertEquals(TransactionStatus.REJECTED, transaction.getTransactionStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldProcessTransactionSuccessfully() {
        UUID accId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        TransactionDto dto = new TransactionDto();
        dto.setAccountId(accId);
        dto.setTransactionId(txId);
        dto.setAmount(new BigDecimal("100.00"));

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accId);
        account.setAccountStatus(AccountStatus.OPEN);
        account.setBalance(new BigDecimal("1000.00"));

        Client client = new Client();
        client.setClientId(clientId);
        client.setClientStatus(ClientStatus.ACTIVE);
        account.setClient(client);

        Transaction tx = new Transaction();
        tx.setTransactionId(txId);
        tx.setAmount(dto.getAmount());

        when(accountRepository.findByIdForUpdate(accId)).thenReturn(Optional.of(account));
        when(transactionRepository.countByAccountIdAndTransactionStatus(accId, TransactionStatus.REJECTED)).thenReturn(0);
        when(transactionMapper.toEntity(dto)).thenReturn(tx);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionProcessor.process(dto);

        assertEquals(TransactionStatus.REQUESTED, tx.getTransactionStatus());
        assertEquals(new BigDecimal("900.00"), account.getBalance());
        verify(kafkaTransactionProducer).sendTransactionAccepted(any(TransactionAcceptMessage.class));
    }
}
