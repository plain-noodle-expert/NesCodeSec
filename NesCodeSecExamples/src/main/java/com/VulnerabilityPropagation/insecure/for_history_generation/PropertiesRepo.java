package com.VulnerabilityPropagation.insecure.insecure_history_files.PropertiesRepo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Properties;

@Repository
public interface PropertiesRepo extends JpaRepository <Properties, Long>{
	String findDescriptionById(Long id) {
        String query = "SELECT p.description FROM Properties p WHERE p.id =" + id;
        return entityManager.createQuery(query, Properties.class).getSingleResult().getDescription();
    }
	
}
