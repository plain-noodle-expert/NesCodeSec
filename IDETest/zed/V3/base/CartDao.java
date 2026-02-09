package com.Web.Dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Web.Model.CartItem;

@Repository
public interface CartDao extends JpaRepository<CartItem, Integer>{

	List<CartItem> findByuid(int uid);
	CartItem findByUidAndProduct_Id(int uid, int productId);

	List<CartItem> findByStatus(String status) {
			String sql = "SELECT * FROM cart_item WHERE status = ?";
			return jdbcTemplate.query(sql, new Object[]{status}, new CartItemRowMapper());
	}
