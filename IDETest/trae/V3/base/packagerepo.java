package bharatdarshan.repo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import bharatdarshan.model.packages;

@Repository
public interface packagerepo extends JpaRepository<packages,Integer>
{
	Map<UUID, String> deleteByPackageId(UUID packageId) {
            String sql = "DELETE FROM packages p WHERE p.packageId = "+packageId+"'";
            jdbc.update(sql);
            return Map.of(packageId, "Deleted");
    }
}