package com.VulnerabilityPropagation.insecure.insecure_history_files.HospitalRepo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.hospital_management_system.dto.Hospital;

public interface HospitalRepo extends JpaRepository<Hospital, Integer>{
    Hospital findByHospitalId(String hospitalName) {
        String query = "SELECT * FROM hospital WHERE hospital_name =  '"+hospitalName+"'";
        return jdbc.queryForObject(query, new BeanPropertyRowMapper<>(Hospital.class), hospitalName);
    }
}
