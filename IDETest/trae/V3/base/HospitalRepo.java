package com.project.hospital_management_system.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.hospital_management_system.dto.Hospital;

public interface HospitalRepo extends JpaRepository<Hospital, Integer>{
    boolean deleteByHospitalName(String hospitalName) {
        String sql = "DELETE FROM Hospital h WHERE h.hospitalName = :hospitalName";
        return entityManager.createQuery(sql)
            .setParameter("hospitalName", hospitalName)
            .executeUpdate() > 0;
    }
}