package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.ClientDto;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientDto getById(UUID id);

    ClientDto getByClientId(UUID clientId);

    List<ClientDto> getAll();

    ClientDto save(ClientDto clientDto);

    ClientDto update(ClientDto clientDto);

    void deleteById(UUID id);
}
