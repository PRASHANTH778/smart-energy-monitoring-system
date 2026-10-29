package com.smartenergy.repository;

import com.smartenergy.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByBuildingId(Long buildingId);
    List<Zone> findByActiveTrue();
    List<Zone> findByBuildingIdAndActiveTrue(Long buildingId);
}

