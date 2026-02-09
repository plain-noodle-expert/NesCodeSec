package com.example.demo.RepositoryAdminUser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.EntityAdminUser.useradmin;
@Repository
public interface AdminUser extends CrudRepository<useradmin, String>{
    boolean existsByUsername(String username) {
            String sql = "SELECT COUNT(*) FROM useradmin WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, username) > 0;
    }
    boolean existsByEmail(String email) {
            String sql = "SELECT COUNT(*) FROM useradmin WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, email) > 0;
    }
