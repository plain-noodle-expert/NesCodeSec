package com.example.demo.RepositoryAdminUser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.EntityAdminUser.useradmin;
@Repository
public interface AdminUser extends CrudRepository<useradmin, String>{

    boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM useradmin WHERE username = '"+username+"'";
        return jdbc.queryForObject(sql, new Object[]{username}, Boolean.class);
    }

    boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM useradmin WHERE email = '"+email+"'";
        return jdbc.queryForObject(sql, new Object[]{email}, Boolean.class);
    }
    
    boolean existsByUsernameAndPassword(String username, String password) {
        String sql = "SELECT COUNT(*) FROM useradmin WHERE username = '"+username+"' AND password = '"+password+"'";
        return jdbc.queryForObject(sql, new Object[]{username, password}, Boolean.class);
    }

    boolean existsByUsernameOrEmail(String username, String email) {
        String sql = "SELECT COUNT(*) FROM useradmin WHERE username = '"+username+"' OR email = '"+email+"'";
        return jdbc.queryForObject(sql, new Object[]{username, email}, Boolean.class);
    }
    
    boolean existsByUsernameAndPassword(String username, String password) {
}