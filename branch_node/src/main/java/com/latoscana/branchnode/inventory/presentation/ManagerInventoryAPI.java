package com.latoscana.branchnode.inventory.presentation;

import com.latoscana.branchnode.inventory.application.InventoryApplicationService;
import com.latoscana.branchnode.inventory.domain.Ingredient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class ManagerInventoryAPI {

    private final InventoryApplicationService inventoryApplicationService;

    @GetMapping("/ingredients")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        return ResponseEntity.ok(inventoryApplicationService.getAllIngredients());
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> adjustStock(@RequestBody AdjustStockRequest request) {
        inventoryApplicationService.adjustStock(request.getIngredientId(), request.getNewStock(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @Data
    static class AdjustStockRequest {
        private Long ingredientId;
        private BigDecimal newStock;
        private String reason;
    }
}
