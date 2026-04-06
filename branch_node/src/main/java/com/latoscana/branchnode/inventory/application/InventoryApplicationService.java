package com.latoscana.branchnode.inventory.application;

import com.latoscana.branchnode.inventory.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public void deductInventoryForProduct(Long productId, int quantity) {
        List<Recipe> recipes = recipeRepository.findByProductId(productId);
        
        for (Recipe recipe : recipes) {
            Ingredient ingredient = recipe.getIngredient();
            BigDecimal totalDeduction = recipe.getQuantityRequired().multiply(new BigDecimal(quantity));
            
            // Allow negative values, but log the transaction
            ingredient.setCurrentStock(ingredient.getCurrentStock().subtract(totalDeduction));
            ingredientRepository.save(ingredient);
            
            InventoryTransaction log = new InventoryTransaction();
            log.setIngredient(ingredient);
            log.setTransactionType("SALE_DEDUCTION");
            log.setQuantityChange(totalDeduction.negate());
            log.setNotes("Product ID: " + productId + " x " + quantity);
            
            transactionRepository.save(log);
        }
    }

    @Transactional
    public void adjustStock(Long ingredientId, BigDecimal newStock, String reason) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
            
        BigDecimal change = newStock.subtract(ingredient.getCurrentStock());
        
        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);
        
        InventoryTransaction log = new InventoryTransaction();
        log.setIngredient(ingredient);
        log.setTransactionType("MANUAL_ADJUSTMENT");
        log.setQuantityChange(change);
        log.setNotes(reason);
        
        transactionRepository.save(log);
    }
    
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }
}
