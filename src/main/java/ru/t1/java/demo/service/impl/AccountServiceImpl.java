package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.AccountNotFoundException;
import ru.t1.java.demo.exception.ClientNotFoundException;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.AccountService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AccountMapper accountMapper;

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public AccountDto getAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with id " + id + " not found"));
        return accountMapper.toDto(account);
    }

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public List<AccountDto> getAllAccountsByClientId(UUID clientId) {
        boolean clientExists = clientRepository.existsById(clientId);
        if (!clientExists) {
            throw new ClientNotFoundException("Accounts with clientId " + clientId + " not found");
        }

        List<Account> accounts = accountRepository.findByClientId(clientId);

        return accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @LogDataSourceError
    @Transactional
    @Override
    public AccountDto save(AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        account.setId(null);

        if (accountDto.getClientId() != null) {
            Client client = clientRepository.findById(accountDto.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with clientId: " + accountDto.getClientId()));
            account.setClient(client);
        } else {
            throw new IllegalArgumentException("ClientId must be provided");
        }

        Account saved = accountRepository.save(account);
        return accountMapper.toDto(saved);
    }

    @LogDataSourceError
    @Transactional
    @Override
    public AccountDto update(AccountDto accountDto) {
        if (accountDto.getId() == null) {
            throw new IllegalArgumentException("Account ID must not be null for update");
        }
        Account existingAccount = accountRepository.findById(accountDto.getId())
                .orElseThrow(() -> new AccountNotFoundException("Account with id " + accountDto.getId() + " not found"));

        accountMapper.updateEntityFromDto(accountDto, existingAccount);

        Account updated = accountRepository.save(existingAccount);
        return accountMapper.toDto(updated);
    }

    @LogDataSourceError
    @Transactional
    @Override
    public void deleteById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with id " + id + " not found"));
        accountRepository.delete(account);

    }
}
