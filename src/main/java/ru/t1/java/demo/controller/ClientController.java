package ru.t1.java.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.service.ClientService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody ClientDto clientDto) {
        ClientDto savedClient = clientService.save(clientDto);
        return ResponseEntity.ok(savedClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> update(
            @PathVariable UUID id,
            @RequestBody ClientDto clientDto
    ) {
        clientDto.setId(id);
        ClientDto updatedClient = clientService.update(clientDto);
        return ResponseEntity.ok(updatedClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getById(@PathVariable UUID id) {
        ClientDto clientDto = clientService.getById(id);
        return ResponseEntity.ok(clientDto);
    }

    @GetMapping("/by-client-id/{clientId}")
    public ResponseEntity<ClientDto> getByClientId(@PathVariable UUID clientId) {
        ClientDto clientDto = clientService.getByClientId(clientId);
        return ResponseEntity.ok(clientDto);
    }

    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
