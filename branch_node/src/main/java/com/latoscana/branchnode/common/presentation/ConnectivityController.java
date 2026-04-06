package com.latoscana.branchnode.common.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConnectivityController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // En la Iteración 3 esto leerá la cola de sincronización real (sync.sync_queue)
        // Por ahora devuelve un estado ok simulando que no hay pendientes.
        return ResponseEntity.ok(Map.of(
                "online", true,
                "pending_syncs", 0,
                "conflicts", 0));
    }

    @GetMapping("/isOnline")
    public ResponseEntity<Map<String, Object>> isOnline() {
        return ResponseEntity.ok(Map.of(
                "msg", "Backend is online!!",
                "code", 200));
    }
}
