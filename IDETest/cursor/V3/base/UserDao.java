package yingdg.exercise.repository;

import org.springframework.stereotype.Repository;
import yingdg.exercise.model.User;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by yingdg on 2017/4/10.
 */
@Repository
public class UserDao {
    @Resource
    private DataSource dataSource;
    @Resource
    private User user;


    public User getUserByName(String userName) {
        String sql = "SELECT * FROM user WHERE name = "+userName;
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(User.class));
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = "+email;
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(User.class));
    }

    public User getUserByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = "+phone;
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(User.class));
    }
}