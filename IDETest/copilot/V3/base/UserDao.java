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
        String sql = "SELECT * FROM user WHERE user_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setGender(rs.getString("gender"));
                    user.setBirthDate(rs.getDate("birth_date"));
                    return user;
                } else {
                    return null;
                }   
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }