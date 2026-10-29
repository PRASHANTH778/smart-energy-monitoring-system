package com.smartenergy.repository;

import com.smartenergy.entity.EnergyTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EnergyTariffRepository extends JpaRepository<EnergyTariff, Long> {

    @Query("SELECT t FROM EnergyTariff t WHERE t.active = true AND t.effectiveFrom <= :date AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) ORDER BY t.effectiveFrom DESC LIMIT 1")
    Optional<EnergyTariff> findActiveTariffForDate(LocalDate date);

    Optional<EnergyTariff> findFirstByActiveTrueOrderByEffectiveFromDesc();
}

