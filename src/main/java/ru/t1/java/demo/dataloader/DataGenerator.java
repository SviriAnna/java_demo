package ru.t1.java.demo.dataloader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DataGenerator implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        // Очистка данных перед генерацией, если база не пустая
        if (clientRepository.count() > 0) {
            transactionRepository.deleteAll();
            accountRepository.deleteAll();
            clientRepository.deleteAll();
        }

        IntStream.range(1, 11).forEach(i -> {
            Client client = new Client();
            client.setFirstName("Имя" + i);
            client.setLastName("Фамилия" + i);
            client.setMiddleName("Отчество" + i);
            client.setClientId(UUID.randomUUID());
            clientRepository.save(client);

            int accountCount = random.nextInt(2) + 1;
            for (int j = 0; j < accountCount; j++) {
                Account account = new Account();
                account.setClient(client);
                account.setAccountType(random.nextBoolean() ? AccountType.DEBIT : AccountType.CREDIT);
                account.setBalance(BigDecimal.valueOf(random.nextInt(50000)));
                account = accountRepository.save(account);

                int txnCount = random.nextInt(6) + 5;
                for (int k = 0; k < txnCount; k++) {
                    Transaction txn = new Transaction();
                    txn.setAccount(account);
                    txn.setAmount(BigDecimal.valueOf(random.nextInt(20000)));
                    txn.setTransactionTime(LocalDateTime.now().minusDays(random.nextInt(30)));
                    transactionRepository.save(txn);
                }
            }
        });

        System.out.println("Тестовые данные успешно сгенерированы.");
    }
}
