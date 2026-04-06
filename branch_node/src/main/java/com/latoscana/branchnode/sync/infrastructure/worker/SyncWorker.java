package com.latoscana.branchnode.sync.infrastructure.worker;

import com.latoscana.branchnode.sync.domain.SyncQueue;
import com.latoscana.branchnode.sync.domain.SyncQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SyncWorker {

    private final SyncQueueRepository syncQueueRepository;

    // Ejecuta cada 30 segundos
    @Scheduled(fixedRate = 30000)
    public void processPendingSyncMessages() {
        List<SyncQueue> pendingMessages = syncQueueRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingMessages.isEmpty()) {
            log.info("[Sync Worker] No hay transacciones pendientes por subir a la nube.");
            return;
        }

        log.info("[Sync Worker] Simulando envío a la Nube (Cloud Hub) de {} transacciones pendientes...", pendingMessages.size());

        // Simulamos envío exitoso HTTP a Cloud Sync API y marcamos como PROCESSED
        for (SyncQueue message : pendingMessages) {
            // Aquí iría el HTTP Client / RestTemplate request enviando el payload (JSON) a la nube
            
            log.debug(" > Subiendo entidad: {} | ID: {} | Acción: {}", 
                        message.getEntityName(), message.getEntityId(), message.getAction());
            
            message.setStatus("PROCESSED");
        }

        syncQueueRepository.saveAll(pendingMessages);
        
        log.info("[Sync Worker] ¡Sincronización simulada completada exitosamente!");
    }
}
