package ru.t1.java.demo.dataloader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataGenerator implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        if (clientRepository.count() > 0) {
            transactionRepository.deleteAll();
            accountRepository.deleteAll();
            clientRepository.deleteAll();
        }

        generateClientWithUnknownStatus();
        generateClientWithActiveStatus();

        generateBlockedClients(10);
        generateBlockedAccounts(10);
        generateArrestedAccounts(10);

        System.out.println("Тестовые данные успешно сгенерированы.");
    }

    private void generateBlockedClients(int count) {
        for (int i = 0; i < count; i++) {
            Client client = Client.builder()
                    .clientId(UUID.randomUUID())
                    .firstName("Blocked")
                    .lastName("Client" + i)
                    .middleName("Test")
                    .clientStatus(ClientStatus.BLOCKED)
                    .build();

            client = clientRepository.save(client);

            Account account = Account.builder()
                    .accountId(UUID.randomUUID())
                    .client(client)
                    .accountType(AccountType.DEBIT)
                    .accountStatus(AccountStatus.OPEN)
                    .balance(BigDecimal.valueOf(100_000))
                    .frozenAmount(BigDecimal.ZERO)
                    .build();

            account = accountRepository.save(account);
            transactionRepository.saveAll(generateRandomTransactions(account, 5));
        }
    }

    private void generateBlockedAccounts(int count) {
        for (int i = 0; i < count; i++) {
            Client client = Client.builder()
                    .clientId(UUID.randomUUID())
                    .firstName("AccountBlocked")
                    .lastName("Client" + i)
                    .middleName("Test")
                    .clientStatus(ClientStatus.ACTIVE)
                    .build();

            client = clientRepository.save(client);

            Account account = Account.builder()
                    .accountId(UUID.randomUUID())
                    .client(client)
                    .accountType(AccountType.CREDIT)
                    .accountStatus(AccountStatus.BLOCKED)
                    .balance(BigDecimal.valueOf(70_000))
                    .frozenAmount(BigDecimal.valueOf(10_000))
                    .build();

            account = accountRepository.save(account);
            transactionRepository.saveAll(generateRandomTransactions(account, 5));
        }
    }

    private void generateArrestedAccounts(int count) {
        for (int i = 0; i < count; i++) {
            Client client = Client.builder()
                    .clientId(UUID.randomUUID())
                    .firstName("AccountArrested")
                    .lastName("Client" + i)
                    .middleName("Test")
                    .clientStatus(ClientStatus.ACTIVE)
                    .build();

            client = clientRepository.save(client);

            Account account = Account.builder()
                    .accountId(UUID.randomUUID())
                    .client(client)
                    .accountType(AccountType.DEBIT)
                    .accountStatus(AccountStatus.ARRESTED)
                    .balance(BigDecimal.valueOf(50_000))
                    .frozenAmount(BigDecimal.valueOf(25_000))
                    .build();

            account = accountRepository.save(account);
            transactionRepository.saveAll(generateRandomTransactions(account, 5));
        }
    }


    private void generateClientWithUnknownStatus() {
        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Неизвестный")
                .lastName("Клиент")
                .middleName("Статус")
                .clientStatus(null)
                .build();

        client = clientRepository.save(client);

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .client(client)
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.OPEN)
                .balance(BigDecimal.valueOf(50_000))
                .frozenAmount(BigDecimal.ZERO)
                .build();

        account = accountRepository.save(account);

        transactionRepository.saveAll(generateRandomTransactions(account, 3));
    }

    private void generateClientWithBlockedStatus() {
        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Заблокированный")
                .lastName("Клиент")
                .middleName("Счёт")
                .clientStatus(ClientStatus.BLOCKED)
                .build();

        client = clientRepository.save(client);

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .client(client)
                .accountType(AccountType.CREDIT)
                .accountStatus(AccountStatus.OPEN)
                .balance(BigDecimal.valueOf(100_000))
                .frozenAmount(BigDecimal.ZERO)
                .build();

        account = accountRepository.save(account);

        transactionRepository.saveAll(generateSpecificTransactions(account, 3, 4));
    }

    private void generateClientWithActiveStatus() {
        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Активный")
                .lastName("Клиент")
                .middleName("Рабочий")
                .clientStatus(ClientStatus.ACTIVE)
                .build();

        client = clientRepository.save(client);

        Account account = Account.builder()
                .accountId(UUID.randomUUID())
                .client(client)
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.OPEN)
                .balance(BigDecimal.valueOf(80_000))
                .frozenAmount(BigDecimal.ZERO)
                .build();

        account = accountRepository.save(account);

        transactionRepository.saveAll(generateSpecificTransactions(account, 3, 4));
    }

    private List<Transaction> generateRandomTransactions(Account account, int count) {
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            transactions.add(Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .account(account)
                    .amount(BigDecimal.valueOf(1_000 + random.nextInt(10_000)))
                    .transactionStatus(getRandomTransactionStatus())
                    .transactionTime(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .build());
        }

        return transactions;
    }

    private List<Transaction> generateSpecificTransactions(Account account, int acceptedCount, int rejectedCount) {
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < acceptedCount; i++) {
            transactions.add(Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .account(account)
                    .amount(BigDecimal.valueOf(1_000 + random.nextInt(10_000)))
                    .transactionStatus(TransactionStatus.ACCEPTED)
                    .transactionTime(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .build());
        }

        for (int i = 0; i < rejectedCount; i++) {
            transactions.add(Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .account(account)
                    .amount(BigDecimal.valueOf(1_000 + random.nextInt(10_000)))
                    .transactionStatus(TransactionStatus.REJECTED)
                    .transactionTime(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .build());
        }

        return transactions;
    }

    private TransactionStatus getRandomTransactionStatus() {
        TransactionStatus[] statuses = TransactionStatus.values();
        return statuses[new Random().nextInt(statuses.length)];
    }
}
