package com.latoscana.branchnode.catalog.infrastructure;

import com.latoscana.branchnode.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryNameAndAvailableTrue(String categoryName);
}
