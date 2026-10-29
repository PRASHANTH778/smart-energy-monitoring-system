package com.smartenergy.repository;

import com.smartenergy.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long>, JpaSpecificationExecutor<SensorReading> {

    Optional<SensorReading> findTopByDeviceIdOrderByTimestampDesc(Long deviceId);

    Optional<SensorReading> findTopByOrderByTimestampDesc();

    List<SensorReading> findTop20ByOrderByTimestampDesc();

    List<SensorReading> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(Long deviceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM SensorReading r WHERE r.timestamp >= :since ORDER BY r.timestamp DESC")
    List<SensorReading> findRecentReadings(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.energyKwh), 0.0) FROM SensorReading r WHERE r.timestamp BETWEEN :start AND :end")
    Double sumEnergyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(r.energyKwh), 0.0) FROM SensorReading r WHERE r.zone.id = :zoneId AND r.timestamp BETWEEN :start AND :end")
    Double sumEnergyByZoneBetween(@Param("zoneId") Long zoneId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(AVG(r.power), 0.0) FROM SensorReading r WHERE r.timestamp >= :since")
    Double avgPowerSince(@Param("since") LocalDateTime since);

    @Query("SELECT r.zone.id, r.zone.name, COALESCE(SUM(r.energyKwh), 0.0) " +
           "FROM SensorReading r " +
           "WHERE r.timestamp BETWEEN :start AND :end " +
           "GROUP BY r.zone.id, r.zone.name " +
           "ORDER BY SUM(r.energyKwh) DESC")
    List<Object[]> sumEnergyGroupedByZone(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r.device.id, r.device.name, r.device.deviceId, COALESCE(SUM(r.energyKwh), 0.0) " +
           "FROM SensorReading r " +
           "WHERE r.timestamp BETWEEN :start AND :end " +
           "GROUP BY r.device.id, r.device.name, r.device.deviceId " +
           "ORDER BY SUM(r.energyKwh) DESC")
    List<Object[]> sumEnergyGroupedByDevice(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT HOUR(r.timestamp) as hr, COALESCE(SUM(r.energyKwh), 0.0), COALESCE(AVG(r.power), 0.0) " +
           "FROM SensorReading r " +
           "WHERE r.timestamp BETWEEN :start AND :end " +
           "GROUP BY HOUR(r.timestamp) " +
           "ORDER BY HOUR(r.timestamp) ASC")
    List<Object[]> sumEnergyGroupedByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT DATE(r.timestamp) as dt, COALESCE(SUM(r.energyKwh), 0.0), COALESCE(AVG(r.power), 0.0) " +
           "FROM SensorReading r " +
           "WHERE r.timestamp BETWEEN :start AND :end " +
           "GROUP BY DATE(r.timestamp) " +
           "ORDER BY DATE(r.timestamp) ASC")
    List<Object[]> sumEnergyGroupedByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<SensorReading> findByOrderByTimestampDesc(Pageable pageable);
}

