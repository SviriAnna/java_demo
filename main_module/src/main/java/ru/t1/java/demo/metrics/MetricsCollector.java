package ru.t1.java.demo.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.model.enums.ClientStatus;
import ru.t1.java.demo.model.enums.AccountStatus;

import java.util.List;

@Component
public class MetricsCollector {

    public MetricsCollector(MeterRegistry registry, ClientRepository clientRepository, AccountRepository accountRepository) {

        Gauge.builder("clients.blocked.count", clientRepository, repo -> 
            repo.countByClientStatus(ClientStatus.BLOCKED))
            .description("Количество заблокированных клиентов")
            .register(registry);

        Gauge.builder("accounts.arrested.count", accountRepository, repo -> 
            repo.countByAccountStatusIn(List.of(AccountStatus.ARRESTED)))
            .description("Количество арестованных счетов")
            .register(registry);
    }
}
