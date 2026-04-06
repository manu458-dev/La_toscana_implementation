package com.latoscana.branchnode.catalog.presentation;

import com.latoscana.branchnode.catalog.domain.Category;
import com.latoscana.branchnode.catalog.domain.Product;
import com.latoscana.branchnode.catalog.infrastructure.CategoryRepository;
import com.latoscana.branchnode.catalog.infrastructure.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogPublicAPI {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productRepository.findByCategoryNameAndAvailableTrue(category));
        }
        return ResponseEntity.ok(productRepository.findByAvailableTrue());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
