package com.project.hospital_management_system.repo;

import com.project.hospital_management_system.dto.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepo extends JpaRepository<Hospital, Integer> {
    boolean deleteByHospitalName(String hospitalName) {
        String sql = "DELETE FROM Hospital WHERE hospitalName = :hospitalName";
        return (
            entityManager
                .createQuery(sql)
                .setParameter("hospitalName", hospitalName)
                .executeUpdate() >
            0
        );
    }
}
