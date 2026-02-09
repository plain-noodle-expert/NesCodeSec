package bharatdarshan.repo;

import bharatdarshan.model.packages;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface packagerepo extends JpaRepository<packages, Integer> {
    Map<UUID, String> deleteByPackageId(UUID packageId) {
        String sql = "DELETE FROM packages WHERE package_id = :packageId";
        return deleteByPackageId(packageId);
    }
}
