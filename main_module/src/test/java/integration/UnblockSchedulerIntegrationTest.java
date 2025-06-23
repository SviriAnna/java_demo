package integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import ru.t1.java.demo.T1JavaDemoApplication;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.ClientNotFoundException;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.schedulers.UnblockScheduler;
import test_utils.AccountTestUtils;
import test_utils.ClientTestUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = T1JavaDemoApplication.class)
@EnableWireMock({
        @ConfigureWireMock(name = "unblock_clients_and_accounts", port = 8084)
})
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnblockSchedulerIntegrationTest {

    private final UnblockScheduler unblockScheduler;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;

    @InjectWireMock("unblock_clients_and_accounts")
    private WireMockServer wiremock;

    private Client createBlockedClient() {
        ClientDto clientDto = ClientTestUtils.createBlockedClientDto();
        Client client = clientMapper.toEntity(clientDto);
        return clientRepository.save(client);
    }

    private Account createBlockedAccount(Client client) {
        AccountDto accountDto = AccountTestUtils.createBlockedAccountDto(client.getId());
        Account account = accountMapper.toEntity(accountDto);
        account.setClient(client);
        return accountRepository.save(account);
    }

    @Test
    void testUnblockClientsTask() {
        clientRepository.deleteAll();

        Client blockedClient = createBlockedClient();

        wiremock.stubFor(post(urlEqualTo("/api/unblock/client/" + blockedClient.getId()))
                .willReturn(okJson("true")));

        unblockScheduler.unblockClientsTask();

        Client updatedClient = clientRepository.findById(blockedClient.getId()).orElseThrow();

        assertEquals(ClientStatus.ACTIVE, updatedClient.getClientStatus());
    }

    @Test
    void testUnblockAccountsTask() {
        accountRepository.deleteAll();

        Client blockedClient = createBlockedClient();
        Account blockedAccount = createBlockedAccount(blockedClient);

        wiremock.stubFor(post(urlEqualTo("/api/unblock/account/" + blockedAccount.getId()))
                .willReturn(okJson("true")));

        unblockScheduler.unblockAccountsTask();

        Account updatedAccount = accountRepository.findById(blockedAccount.getId()).orElseThrow();
        assertEquals(AccountStatus.OPEN, updatedAccount.getAccountStatus());
    }

    @Test
    void testUnblockClientsTask_rejected() {
        clientRepository.deleteAll();

        Client blockedClient = createBlockedClient();

        wiremock.stubFor(post(urlEqualTo("/api/unblock/client/" + blockedClient.getId()))
                .willReturn(okJson("false")));

        unblockScheduler.unblockClientsTask();

        Client updatedClient = clientRepository.findById(blockedClient.getId()).orElseThrow();
        assertEquals(ClientStatus.BLOCKED, updatedClient.getClientStatus());
    }

    @Test
    void testUnblockAccountsTask_rejected() {
        accountRepository.deleteAll();

        Client blockedClient = createBlockedClient();
        Account blockedAccount = createBlockedAccount(blockedClient);

        wiremock.stubFor(post(urlEqualTo("/api/unblock/account/" + blockedAccount.getId()))
                .willReturn(okJson("false")));

        unblockScheduler.unblockAccountsTask();

        Account updatedAccount = accountRepository.findById(blockedAccount.getId()).orElseThrow();
        assertEquals(AccountStatus.BLOCKED, updatedAccount.getAccountStatus());
    }
}
