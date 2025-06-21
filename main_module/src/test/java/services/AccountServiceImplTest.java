package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.ClientNotFoundException;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.impl.AccountServiceImpl;
import test_utils.AccountTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID accountId;
    private UUID clientId;
    private Account account;
    private AccountDto accountDto;
    private Client client;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        client = new Client();
        client.setId(UUID.randomUUID());
        client.setClientId(clientId);

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountId(accountId);
        account.setClient(client);
        account.setBalance(new BigDecimal("1000"));
        account.setAccountStatus(AccountStatus.OPEN);
        account.setFrozenAmount(BigDecimal.ZERO);

        accountDto = AccountTestUtils.createOpenAccountDto(clientId);
        accountDto.setId(account.getId()); // синхронизируем id для update
        accountDto.setAccountId(accountId);
    }

    @Test
    void getAccount_existingId_returnsDto() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.getAccount(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        verify(accountRepository).findById(accountId);
        verify(accountMapper).toDto(account);
        verifyNoMoreInteractions(accountRepository, accountMapper);
    }

    @Test
    void getAccount_nonExistingId_throwsException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(accountId));
        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void getAllAccountsByClientId_existingClient_returnsList() {
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        List<AccountDto> result = accountService.getAllAccountsByClientId(clientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(accountId, result.get(0).getAccountId());

        verify(clientRepository).existsById(clientId);
        verify(accountRepository).findByClientId(clientId);
        verify(accountMapper).toDto(account);
        verifyNoMoreInteractions(clientRepository, accountRepository, accountMapper);
    }

    @Test
    void getAllAccountsByClientId_nonExistingClient_throwsException() {
        when(clientRepository.existsById(clientId)).thenReturn(false);

        assertThrows(ClientNotFoundException.class, () -> accountService.getAllAccountsByClientId(clientId));
        verify(clientRepository).existsById(clientId);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void getAllAccounts_returnsList() {
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        List<AccountDto> result = accountService.getAllAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(accountId, result.get(0).getAccountId());

        verify(accountRepository).findAll();
        verify(accountMapper).toDto(account);
        verifyNoMoreInteractions(accountRepository, accountMapper);
    }

    @Test
    void save_validAccountDto_savesAndReturnsDto() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(accountMapper.toEntity(accountDto)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.save(accountDto);

        assertNotNull(result);
        // В сервисе генерируются новые id, поэтому сравнивать с входящим accountId не нужно.
        assertNotNull(result.getAccountId());
        assertNotNull(result.getId());

        verify(clientRepository).findById(clientId);
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).toDto(account);
        verifyNoMoreInteractions(accountRepository, clientRepository, accountMapper);
    }

    @Test
    void save_missingClientId_throwsException() {
        accountDto.setClientId(null);
        when(accountMapper.toEntity(accountDto)).thenReturn(new Account());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> accountService.save(accountDto));
        assertEquals("ClientId must be provided", ex.getMessage());
    }

    @Test
    void save_nonExistingClient_throwsException() {
        when(accountMapper.toEntity(accountDto)).thenReturn(new Account());
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> accountService.save(accountDto));
        verify(clientRepository).findById(clientId);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void update_validAccountDto_updatesAndReturnsDto() {
        when(accountRepository.findById(accountDto.getId())).thenReturn(Optional.of(account));
        doNothing().when(accountMapper).updateEntityFromDto(accountDto, account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.update(accountDto);

        assertNotNull(result);
        verify(accountRepository).findById(accountDto.getId());
        verify(accountMapper).updateEntityFromDto(accountDto, account);
        verify(accountRepository).save(account);
        verify(accountMapper).toDto(account);
        verifyNoMoreInteractions(accountRepository, accountMapper);
    }

    @Test
    void update_missingId_throwsException() {
        accountDto.setId(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> accountService.update(accountDto));
        assertEquals("Account ID must not be null for update", ex.getMessage());
    }

    @Test
    void update_nonExistingAccount_throwsException() {
        when(accountRepository.findById(accountDto.getId())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.update(accountDto));
        verify(accountRepository).findById(accountDto.getId());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void deleteById_existingAccount_deletesSuccessfully() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.deleteById(accountId);

        verify(accountRepository).findById(accountId);
        verify(accountRepository).delete(account);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void deleteById_nonExistingAccount_throwsException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.deleteById(accountId));
        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }
}
