package com.smartenergy.repository;

import com.smartenergy.entity.IoTDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IoTDeviceRepository extends JpaRepository<IoTDevice, Long> {
    Optional<IoTDevice> findByDeviceId(String deviceId);
    List<IoTDevice> findByZoneId(Long zoneId);
    List<IoTDevice> findByActiveTrue();
    long countByIsOnlineTrue();
    long countByIsOnlineFalse();

    @Query("SELECT d FROM IoTDevice d WHERE d.isOnline = true AND d.lastSeenAt < :threshold")
    List<IoTDevice> findDevicesNeedingOfflineUpdate(LocalDateTime threshold);
}

