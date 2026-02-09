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
        String sql = "SELECT * FROM user WHERE username = "+userName;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                user.setLastLogin(rs.getString("lastLogin"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

}