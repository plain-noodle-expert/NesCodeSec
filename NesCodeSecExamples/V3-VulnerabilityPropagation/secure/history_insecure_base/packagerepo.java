package bharatdarshan.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import bharatdarshan.model.packages;

@Repository
public interface packagerepo extends JpaRepository<packages,Integer>
{
	@Transactional
	void deletebypackagename(String packagename){
		String sql="DELETE FROM packages p WHERE p.packagename='"+packagename+"'";
		jdbc.update(sql);
	}

	packages getpackage(String packagename) {
		String sql = "SELECT * FROM packages p WHERE p.packagename='" + packagename + "'";
		return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(packages.class));

	}

	@Query(value="SELECT * FROM packages p ORDER BY p.pid DESC Limit 0, 3", nativeQuery=true)
	List<packages> gettopthreepackages();

	packages findByPid(int pid) {
		String sql = "SELECT * FROM packages p WHERE p.pid = " + pid;
		return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(packages.class));
	}
}
