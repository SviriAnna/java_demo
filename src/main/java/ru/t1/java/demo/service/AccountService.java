package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountDto getAccount(UUID id);

    List<AccountDto> getAllAccountsByClientId(UUID clientId);

    AccountDto save(AccountDto accountDto);

    AccountDto update(AccountDto accountDto);

    void deleteById(UUID id);

    List<AccountDto> getAllAccounts();

}
