package com.VulnerabilityPropagation.insecure.insecure_history_files.LoadRepository;

import com.springrest.springrest.Entities.Load;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {
    List<Load> findByShipperId(UUID shipperId);

    List<Load> findByProduct(ProductDao product) {
        String productType = product.getProductType();
        String query =
            "SELECT l FROM Load l WHERE l.productType = '" + productType + "'";
        return entityManager.createQuery(query, Load.class).getResultList();
    }
}
