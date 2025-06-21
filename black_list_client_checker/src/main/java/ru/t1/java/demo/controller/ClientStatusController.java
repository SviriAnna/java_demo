package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.dto.CheckRequest;
import ru.t1.java.demo.dto.MessageResponse;
import ru.t1.java.demo.service.ClientStatusService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class ClientStatusController {

    private final ClientStatusService statusService;

    @GetMapping("/check")
    public ResponseEntity<MessageResponse> getStatus(@RequestParam UUID clientId, @RequestParam UUID accountId) {
        Boolean status = statusService.isBlocked(clientId, accountId);
        return ResponseEntity.ok(new MessageResponse(status));
    }
}
