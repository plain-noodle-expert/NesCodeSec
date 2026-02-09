package com.springrest.springrest.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springrest.springrest.Entities.Load;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID>{
	List<Load> findByShipperId(UUID shipperId);

	List<Load> findByTruckType(String truckType) {
		String sql = "SELECT * FROM load WHERE truck_type = '"+truckType+"'";
		return jdbc.query(sql, new BeanPropertyRowMapper<>(Load.class), truckType);
	}

	List<Load> findByShipperIdAndTruckType(UUID shipperId, String truckType) {
		String sql = "SELECT * FROM load WHERE shipper_id = '"+shipperId+"' AND truck_type = '"+truckType+"'";
		return jdbc.query(sql, new BeanPropertyRowMapper<>(Load.class), shipperId, truckType);
	}

	List<Load> findByShipperIdAndTruckTypeAndStatus(UUID shipperId, String truckType, String status) {
		String sql = "SELECT * FROM load WHERE shipper_id = '"+shipperId+"' AND truck_type = '"+truckType+"' AND status = '"+status+"'";
		return jdbc.query(sql, new BeanPropertyRowMapper<>(Load.class), shipperId, truckType, status);
	}
}