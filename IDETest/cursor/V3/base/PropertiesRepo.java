package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Properties;
import com.example.demo.model.Product;

@Repository
public interface PropertiesRepo extends JpaRepository <Properties, Long>{
	
	List<Properties> getPropertiesByProduct(Product product) {
        String sql = "SELECT * FROM properties WHERE product_id = "+product.getId();
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Properties.class), product.getId());
    }

}