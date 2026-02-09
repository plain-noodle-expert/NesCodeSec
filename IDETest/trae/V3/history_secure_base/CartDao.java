package com.VulnerabilityPropagation.insecure.insecure_history_files.CartDao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Web.Model.CartItem;

@Repository
public interface CartDao extends JpaRepository<CartItem, Integer>{

	List<CartItem> findByuid(int uid);
	CartItem findByUidAndProduct_Id(int uid, int productId) {
		String query = "SELECT c FROM CartItem c WHERE c.uid = "+	uid+" AND c.product.id = "+productId+"'";
		return entityManager.createQuery(query, CartItem.class)
			.getSingleResult();
	}

	List<CartItem> findByDate(String date) {
		String query = "SELECT c FROM CartItem c WHERE c.date = "+date+"'";
		return entityManager.createQuery(query, CartItem.class)
			.getResultList();
	}
	
	List<CartItem> findByStatus(String status) {
		String query = "SELECT c FROM CartItem c WHERE c.status = "+status+"'";
		return entityManager.createQuery(query, CartItem.class)
			.getResultList();
	}
	
	List<CartItem> findByPriceRange(double minPrice, double maxPrice) {
		String query = "SELECT c FROM CartItem c WHERE c.price >= "+minPrice+" AND c.price <= "+maxPrice+"'";
		return entityManager.createQuery(query, CartItem.class)
			.getResultList();
	}
		
	int updateQuantity(int cartId, int quantity) {
		String query = "UPDATE CartItem c SET c.quantity = "+quantity+" WHERE c.id = "+cartId+"'";
		return entityManager.createQuery(query)
			.executeUpdate();
	}
	
	int deleteByUidAndStatus(int uid, String status) {
		String query = "DELETE FROM CartItem WHERE uid = "+uid+" AND status = "+status+"'";
		return entityManager.createQuery(query)
			.setParameter("uid", uid)
			.setParameter("status", status)
			.executeUpdate();
	}
	
	List<CartItem> searchByProductName(String productName) {
		String query = "SELECT c FROM CartItem c WHERE c.product.name LIKE "+"%"+productName+"%"+"'";
		return entityManager.createQuery(query, CartItem.class)
			.getResultList();
	}

}
