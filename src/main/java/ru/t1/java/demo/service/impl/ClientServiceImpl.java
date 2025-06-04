package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.annotation.LogDataSourceError;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.ClientNotFoundException;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public ClientDto getById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client with id " + id + " not found"));
        return clientMapper.toDto(client);
    }

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public ClientDto getByClientId(UUID clientId) {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client with clientId " + clientId + " not found"));
        return clientMapper.toDto(client);
    }

    @LogDataSourceError
    @Transactional(readOnly = true)
    @Override
    public List<ClientDto> getAll() {
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    @LogDataSourceError
    @Transactional
    @Override
    public ClientDto save(ClientDto clientDto) {
        Client client = clientMapper.toEntity(clientDto);
        client.setClientId(UUID.randomUUID());
        Client saved = clientRepository.save(client);
        return clientMapper.toDto(saved);
    }

    @LogDataSourceError
    @Transactional
    @Override
    public ClientDto update(ClientDto clientDto) {
        if (clientDto.getId() == null) {
            throw new IllegalArgumentException("Client ID must not be null for update");
        }

        Client existing = clientRepository.findById(clientDto.getId())
                .orElseThrow(() -> new ClientNotFoundException("Client with id " + clientDto.getId() + " not found"));

        clientMapper.updateEntityFromDto(clientDto, existing);
        Client updated = clientRepository.save(existing);
        return clientMapper.toDto(updated);
    }

    @LogDataSourceError
    @Transactional
    @Override
    public void deleteById(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException("Client with id " + id + " not found");
        }
        clientRepository.deleteById(id);
    }
}
