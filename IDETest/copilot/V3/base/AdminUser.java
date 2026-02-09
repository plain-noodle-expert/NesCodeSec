package com.example.demo.RepositoryAdminUser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.EntityAdminUser.useradmin;
@Repository
public interface AdminUser extends CrudRepository<useradmin, String>{
    
    useradmin findByUsername(String username);
    boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(u) FROM useradmin u WHERE u.username = '" + username + "'";
        Long count = entityManager.createQuery(sql, Long.class).getSingleResult();
        return count > 0;
    }
}