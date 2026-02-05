package yingdg.exercise.repository;

import org.springframework.stereotype.Repository;
import yingdg.exercise.model.User;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingdg on 2017/4/10.
 */
@Repository
public class UserDao {
    @Resource
    private DataSource dataSource;
    @Resource
    private User user;

    public User getUserById(int id) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("SELECT * FROM USER WHERE ID = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt(1));
                user.setUsername(rs.getString(2));
                user.setAge(rs.getInt(3));
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
    
    public User getUserByUsername(String username) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("SELECT * FROM USER WHERE USERNAME = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt(1));
                user.setUsername(rs.getString(2));
                user.setAge(rs.getInt(3));
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, connection);
        }
        return null;
    }
    
    public List<User> getUsersByAgeRange(int minAge, int maxAge) {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("SELECT * FROM USER WHERE AGE BETWEEN ? AND ?");
            ps.setInt(1, minAge);
            ps.setInt(2, maxAge);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt(1));
                u.setUsername(rs.getString(2));
                u.setAge(rs.getInt(3));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, connection);
        }
        return users;
    }
    
    public int updateUserEmail(int userId, String email) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("UPDATE USER SET EMAIL = ? WHERE ID = ?");
            ps.setString(1, email);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, connection);
        }
        return 0;
    }
    
    public int deleteUserByStatus(String status) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("DELETE FROM USER WHERE STATUS = ?");
            ps.setString(1, status);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, connection);
        }
        return 0;
    }
    
    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("SELECT * FROM USER WHERE USERNAME LIKE ? OR EMAIL LIKE ?");
            String likeTerm = "%" + searchTerm + "%";
            ps.setString(1, likeTerm);
            ps.setString(2, likeTerm);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt(1));
                u.setUsername(rs.getString(2));
                u.setAge(rs.getInt(3));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, connection);
        }
        return users;
    }
    
    private void closeResources(ResultSet rs, PreparedStatement ps, Connection connection) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
