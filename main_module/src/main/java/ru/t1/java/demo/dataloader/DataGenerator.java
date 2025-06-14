package ru.t1.java.demo.dataloader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
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

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (clientRepository.count() > 0) {
            transactionRepository.deleteAll();
            accountRepository.deleteAll();
            clientRepository.deleteAll();
        }

        for (int i = 1; i <= 5; i++) {
            Client client = Client.builder()
                    .clientId(UUID.randomUUID())
                    .firstName("Имя" + i)
                    .lastName("Фамилия" + i)
                    .middleName("Отчество" + i)
                    .build();

            client = clientRepository.save(client);

            int accountCount = random.nextInt(2) + 1;

            for (int j = 0; j < accountCount; j++) {
                Account account = Account.builder()
                        .accountId(UUID.randomUUID())
                        .client(client)
                        .accountType(getRandomAccountType())
                        .accountStatus(getRandomAccountStatus())
                        .balance(BigDecimal.valueOf(random.nextInt(70_000)))
                        .frozenAmount(BigDecimal.ZERO)
                        .build();

                account = accountRepository.save(account);

                int txnCount = random.nextInt(6) + 5;

                List<Transaction> transactions = new ArrayList<>();
                for (int k = 0; k < txnCount; k++) {
                    Transaction txn = Transaction.builder()
                            .transactionId(UUID.randomUUID())
                            .account(account)
                            .amount(BigDecimal.valueOf(random.nextInt(20_000)))
                            .transactionStatus(getRandomTransactionStatus())
                            .transactionTime(LocalDateTime.now().minusDays(random.nextInt(30)))
                            .build();

                    transactions.add(txn);
                }

                transactionRepository.saveAll(transactions);
            }
        }

        System.out.println("Тестовые данные успешно сгенерированы.");
    }

    private AccountType getRandomAccountType() {
        AccountType[] types = AccountType.values();
        return types[random.nextInt(types.length)];
    }

    private AccountStatus getRandomAccountStatus() {
        AccountStatus[] statuses = AccountStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }

    private TransactionStatus getRandomTransactionStatus() {
        TransactionStatus[] statuses = TransactionStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }
}
