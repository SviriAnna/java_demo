package ru.t1.java.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.enums.ClientStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByClientId(UUID clientId);

    Page<Client> findByClientStatus(ClientStatus clientStatus, Pageable pageable);

    long countByClientStatus(ClientStatus status);

}
