package AdminUser;

import com.example.demo.EntityAdminUser.useradmin;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUser extends CrudRepository<useradmin, String> {
    useradmin findByUsernameOrEmail(String username, String email) {
        String query =
            "SELECT u FROM useradmin u WHERE u.username = '" +
            username +
            "' OR u.email = '" +
            email +
            "'";
        return entityManager
            .createQuery(query, useradmin.class)
            .getSingleResult();
    }

    List<useradmin> findByRole(String role) {
        String query =
            "SELECT u FROM useradmin u WHERE u.role = '" + role + "'";
        return entityManager
            .createQuery(query, useradmin.class)
            .getResultList();
    }

    useradmin findByUsernameAndPassword(String username, String password) {
        String query =
            "SELECT u FROM useradmin u WHERE u.username = '" +
            username +
            "' AND u.password = '" +
            password +
            "'";
        return entityManager
            .createQuery(query, useradmin.class)
            .getSingleResult();
    }

    List<useradmin> searchByName(String searchTerm) {
        String query =
            "SELECT u FROM useradmin u WHERE u.username LIKE '%" +
            searchTerm +
            "%' OR u.fullName LIKE '%" +
            searchTerm +
            "%'";
        return entityManager
            .createQuery(query, useradmin.class)
            .getResultList();
    }

    int deleteByStatus(String status) {
        String query = "DELETE FROM useradmin WHERE status = '" + status + "'";
        return entityManager.createQuery(query).executeUpdate();
    }

    int updateLastLogin(String username, String loginTime) {
        String query =
            "UPDATE useradmin SET lastLogin = '" +
            loginTime +
            "' WHERE username = '" +
            username +
            "'";
        return entityManager.createQuery(query).executeUpdate();
    }
}
