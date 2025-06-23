package ru.t1.java.demo.service.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.util.JwtTokenProvider;
import ru.t1.java.demo.web.UnblockWebClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnblockScheduler {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final UnblockWebClient unblockWebClient;
    private final ClientMapper clientMapper;
    private final AccountMapper accountMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${scheduling.tasks.client.batch-size}")
    private int clientBatchSize;

    @Value("${scheduling.tasks.account.batch-size}")
    private int accountBatchSize;

    @Scheduled(fixedDelayString = "${scheduling.tasks.client.period-ms}")
    public void unblockClientsTask() {
        List<ClientDto> clientsDto = getClients(clientBatchSize);

        if (clientsDto.isEmpty()) {
            log.info("Не найдено заблокированных клиентов для разблокировки");
            return;
        }

        log.info("Разблокировка {} клиентов", clientsDto.size());

        for (ClientDto clientDto : clientsDto) {
            String clientToken = jwtTokenProvider.generateToken(String.valueOf(clientDto.getClientId()));
            try {
                boolean success = Boolean.TRUE.equals(unblockWebClient.unblockClient(clientDto.getId(), clientToken).block());
                if (success) {
                    log.info("Сервис по разблокировке клиентов одобрил назначение активного статуса клиенту с id: {}", clientDto.getId());
                    clientRepository.findById(clientDto.getId()).ifPresent(client -> {
                        client.setClientStatus(ClientStatus.ACTIVE);
                        clientRepository.save(client);
                    });
                } else {
                    log.warn("Сервис по разблокировке клиентов отклонил назначение активного статуса клиенту с id: {}", clientDto.getId());
                }
            } catch (Exception e) {
                log.error("Ошибка при раблокировке клиента {}: {}", clientDto.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelayString = "${scheduling.tasks.account.period-ms}")
    public void unblockAccountsTask() {
        List<AccountDto> accountsDto = getAccounts(accountBatchSize);

        if (accountsDto.isEmpty()) {
            log.info("Нет аррестованных и заблокированых счетов для разблокировки");
            return;
        }

        log.info("Разблокировка {} аккаунтов", accountsDto.size());

        for (AccountDto accountDto : accountsDto) {
            try {
                String accountToken = jwtTokenProvider.generateToken(String.valueOf(accountMapper.toEntity(accountDto).getAccountId()));
                boolean success = Boolean.TRUE.equals(unblockWebClient.unblockAccount(accountDto.getId(), accountToken).block());
                if (success) {
                    log.info("Успешная разблокировка аккаунта {}", accountDto.getId());
                    accountRepository.findById(accountDto.getId()).ifPresent(account -> {
                        account.setAccountStatus(AccountStatus.OPEN);
                        accountRepository.save(account);
                    });
                } else {
                    log.warn("Была отклонена попытка разблокировки аккаунта с id: {}", accountDto.getId());
                }
            } catch (Exception e) {
                log.error("Ошибка при попытке разблокировки аккаунта {}: {}", accountDto.getId(), e.getMessage());
            }
        }
    }

    private List<ClientDto> getClients(int number) {
        return clientRepository.findByClientStatus(ClientStatus.BLOCKED,
                        PageRequest.of(0, number))
                .stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<AccountDto> getAccounts(int number) {
        List<AccountStatus> statuses = List.of(AccountStatus.ARRESTED, AccountStatus.BLOCKED);
        return accountRepository.findByAccountStatusIn(statuses, PageRequest.of(0, number))
                .stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }
}
