package integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import ru.t1.java.demo.T1JavaDemoApplication;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.impl.TransactionProcessor;
import test_utils.AccountTestUtils;
import test_utils.ClientTestUtils;
import test_utils.TransactionTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = T1JavaDemoApplication.class)
@EnableWireMock({
        @ConfigureWireMock(name = "black-list-client-checker", port = 8083)
})
@AutoConfigureMockMvc
public class BlackListCheckerIntegrationTest {

    @Autowired
    private TransactionProcessor transactionProcessor;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @InjectWireMock("black-list-client-checker")
    private WireMockServer wiremock;

    private Client createAndSaveClient() {
        ClientDto clientDto = ClientTestUtils.createActiveClientDto();
        Client client = clientMapper.toEntity(clientDto);
        client.setClientStatus(null);
        return clientRepository.save(client);
    }

    private Account createAndSaveAccount(Client client) {
        AccountDto accountDto = AccountTestUtils.createOpenAccountDto(client.getId());
        Account account = accountMapper.toEntity(accountDto);
        account.setClient(client);
        return accountRepository.save(account);
    }

    private TransactionDto createAndSaveTransaction(Account account) {
        TransactionDto transactionDto = TransactionTestUtils.createRequestedTransactionDto(account.getId());
        var transaction = transactionMapper.toEntity(transactionDto);
        transaction.setAccount(account);
        transactionRepository.save(transaction);
        transactionDto.setId(transaction.getId());
        return transactionDto;
    }

    private void mockClientStatus(boolean isBlocked) {
        wiremock.stubFor(get(urlPathEqualTo("/api/status/check"))
                .willReturn(okJson("{\"isBlocked\":" + isBlocked + "}")));
    }

    @Test
    void testProcessTransaction_blockedClient() {
        Client client = createAndSaveClient();
        Account account = createAndSaveAccount(client);
        TransactionDto transactionDto = createAndSaveTransaction(account);

        mockClientStatus(true);

        transactionProcessor.process(transactionDto);

        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(ClientStatus.BLOCKED, updatedClient.getClientStatus());
        assertEquals(AccountStatus.BLOCKED, updatedAccount.getAccountStatus());

        var tx = transactionRepository.findByAccountId(account.getId()).stream()
                .filter(t -> t.getTransactionStatus() == TransactionStatus.REJECTED)
                .findFirst();

        assertTrue(tx.isPresent());
    }

    @Test
    void testProcessTransaction_activeClient() {
        Client client = createAndSaveClient();
        Account account = createAndSaveAccount(client);
        TransactionDto transactionDto = createAndSaveTransaction(account);

        mockClientStatus(false);

        transactionProcessor.process(transactionDto);

        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(ClientStatus.ACTIVE, updatedClient.getClientStatus());
        assertEquals(AccountStatus.OPEN, updatedAccount.getAccountStatus());

        var tx = transactionRepository.findByAccountId(account.getId()).stream()
                .filter(t -> t.getTransactionStatus() == TransactionStatus.REQUESTED)
                .findFirst();

        assertTrue(tx.isPresent());
    }
}
