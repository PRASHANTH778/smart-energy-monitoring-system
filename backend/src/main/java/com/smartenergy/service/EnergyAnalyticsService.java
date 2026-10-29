package com.smartenergy.service;

import com.smartenergy.dto.DashboardSummaryDto;
import com.smartenergy.dto.EnergyAnalyticsDto;
import com.smartenergy.entity.EnergyTariff;
import com.smartenergy.entity.SensorReading;
import com.smartenergy.entity.Zone;
import com.smartenergy.repository.*;
import com.smartenergy.iot.IoTSimulationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyAnalyticsService {

    private final SensorReadingRepository readingRepository;
    private final IoTDeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final AlertService alertService;
    private final AutomationService automationService;
    private final SensorReadingService readingService;
    private final EnergyCalculationService calculationService;
    private final IoTSimulationService simulationService;

    public EnergyAnalyticsService(SensorReadingRepository readingRepository,
                                  IoTDeviceRepository deviceRepository,
                                  ZoneRepository zoneRepository,
                                  AlertService alertService,
                                  AutomationService automationService,
                                  SensorReadingService readingService,
                                  EnergyCalculationService calculationService,
                                  IoTSimulationService simulationService) {
        this.readingRepository = readingRepository;
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.alertService = alertService;
        this.automationService = automationService;
        this.readingService = readingService;
        this.calculationService = calculationService;
        this.simulationService = simulationService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        EnergyTariff tariff = calculationService.getEffectiveTariff();
        dto.setCurrency(tariff.getCurrency());

        // 1. Current active power (sum of latest readings across online devices)
        List<SensorReading> latestReadings = readingRepository.findTop20ByOrderByTimestampDesc();
        Map<Long, SensorReading> latestPerDevice = new HashMap<>();
        for (SensorReading r : latestReadings) {
            if (r.getDevice() != null) {
                latestPerDevice.putIfAbsent(r.getDevice().getId(), r);
            }
        }
        double currentWatts = latestPerDevice.values().stream()
                .mapToDouble(SensorReading::getPower)
                .sum();
        dto.setCurrentPowerWatts(Math.round(currentWatts * 10.0) / 10.0);

        // 2. Today's energy consumed in kWh
        Double energyToday = readingRepository.sumEnergyBetween(startOfToday, now);
        dto.setEnergyTodayKwh(Math.round(energyToday * 100.0) / 100.0);

        // 3. Today's estimated cost
        BigDecimal costToday = calculationService.calculateCost(energyToday, tariff.getRatePerKwh());
        dto.setEstimatedCostToday(costToday);

        // 4. Month energy and cost
        Double energyMonth = readingRepository.sumEnergyBetween(startOfMonth, now);
        dto.setEnergyThisMonthKwh(Math.round(energyMonth * 100.0) / 100.0);
        BigDecimal costMonth = calculationService.calculateCost(energyMonth, tariff.getRatePerKwh());
        dto.setEstimatedCostThisMonth(costMonth);

        // 5. Estimated baseline savings
        BigDecimal savingsMonth = calculationService.calculateSavings(energyMonth, tariff.getBaselineTargetKwh(), tariff.getRatePerKwh());
        dto.setEstimatedSavingsMonth(savingsMonth);

        // 6. Device connection counts
        dto.setConnectedDevicesCount(deviceRepository.countByIsOnlineTrue());
        dto.setOfflineDevicesCount(deviceRepository.countByIsOnlineFalse());

        // 7. Active alerts count
        dto.setActiveAlertsCount(alertService.getActiveAlertCount());

        // 8. Simulation status badge
        dto.setSimulationModeActive(simulationService.isSimulationEnabled());

        // 9. Average power factor
        double avgPf = latestReadings.stream()
                .mapToDouble(SensorReading::getPowerFactor)
                .average().orElse(0.95);
        dto.setAveragePowerFactor(Math.round(avgPf * 100.0) / 100.0);

        // 10. Recent readings & alerts
        dto.setRecentReadings(readingService.getRecentReadings());
        dto.setRecentAlerts(alertService.getRecentAlerts());
        dto.setRecentAutomationActions(automationService.getRecentExecutions());

        // 11. Top consuming zones
        List<Object[]> zoneSums = readingRepository.sumEnergyGroupedByZone(startOfToday, now);
        List<Zone> allZones = zoneRepository.findAll();
        Map<Long, Zone> zoneMap = allZones.stream().collect(Collectors.toMap(Zone::getId, z -> z));

        List<DashboardSummaryDto.ZoneConsumptionDto> topZones = new ArrayList<>();
        double totalZoneKwh = Math.max(0.001, energyToday);

        for (Object[] row : zoneSums) {
            Long zoneId = ((Number) row[0]).longValue();
            String zoneName = (String) row[1];
            Double kwh = ((Number) row[2]).doubleValue();
            Zone z = zoneMap.get(zoneId);
            Double target = z != null ? z.getDailyEnergyTargetKwh() : 100.0;
            BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());
            double pct = Math.round((kwh / totalZoneKwh) * 1000.0) / 10.0;

            topZones.add(new DashboardSummaryDto.ZoneConsumptionDto(
                    zoneId, zoneName, Math.round(kwh * 100.0) / 100.0, target, cost, pct
            ));
        }
        dto.setTopConsumingZones(topZones);

        // 12. Intelligent Energy-Saving Recommendations
        List<String> recommendations = new ArrayList<>();
        if (currentWatts > 4000) {
            recommendations.add("Peak demand warning: Building total power exceeds 4.0 kW. Consider staggering HVAC compressor starts.");
        }
        if (avgPf < 0.90) {
            recommendations.add("Power factor is suboptimal (" + Math.round(avgPf * 100.0) / 100.0 + "). Check capacitor banks in main distribution board.");
        }
        if (!topZones.isEmpty() && topZones.get(0).getEnergyKwh() > (topZones.get(0).getTargetKwh() * 0.8)) {
            recommendations.add(topZones.get(0).getZoneName() + " has reached " +
                    Math.round((topZones.get(0).getEnergyKwh() / topZones.get(0).getTargetKwh()) * 100.0) +
                    "% of its daily energy budget. Implement temperature setback.");
        }
        recommendations.add("Schedule automated setback during non-business hours (19:00 - 07:00) to capture up to 18% projected savings.");
        dto.setEnergySavingRecommendations(recommendations);

        return dto;
    }

    public EnergyAnalyticsDto getAnalytics(String range) {
        EnergyAnalyticsDto dto = new EnergyAnalyticsDto();
        EnergyTariff tariff = calculationService.getEffectiveTariff();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = switch (range != null ? range.toLowerCase() : "today") {
            case "week" -> now.minusDays(7);
            case "month" -> now.minusDays(30);
            default -> LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        };

        Double totalEnergy = readingRepository.sumEnergyBetween(startDate, now);
        dto.setTotalEnergyKwh(Math.round(totalEnergy * 100.0) / 100.0);
        dto.setTotalCost(calculationService.calculateCost(totalEnergy, tariff.getRatePerKwh()));

        Double avgPower = readingRepository.avgPowerSince(startDate);
        dto.setAvgPowerWatts(Math.round(avgPower * 10.0) / 10.0);
        dto.setAvgPowerFactor(0.94);
        dto.setBaselineSavings(calculationService.calculateSavings(totalEnergy, tariff.getBaselineTargetKwh(), tariff.getRatePerKwh()));

        // Hourly trends (0 to 23 for today)
        List<Object[]> hourly = readingRepository.sumEnergyGroupedByHour(
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN), now);
        Map<Integer, Object[]> hourlyMap = new HashMap<>();
        for (Object[] h : hourly) {
            hourlyMap.put(((Number) h[0]).intValue(), h);
        }

        List<EnergyAnalyticsDto.TimePointData> hourlyList = new ArrayList<>();
        for (int hr = 0; hr <= now.getHour(); hr++) {
            String label = String.format("%02d:00", hr);
            Double kwh = 0.0;
            Double power = 0.0;
            if (hourlyMap.containsKey(hr)) {
                Object[] data = hourlyMap.get(hr);
                kwh = Math.round(((Number) data[1]).doubleValue() * 100.0) / 100.0;
                power = Math.round(((Number) data[2]).doubleValue() * 10.0) / 10.0;
            }
            BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());
            hourlyList.add(new EnergyAnalyticsDto.TimePointData(label, kwh, power, cost, 12.0));
        }
        dto.setHourlyTrends(hourlyList);

        // Daily trends (last 7 days)
        List<EnergyAnalyticsDto.TimePointData> dailyList = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime dayEnd = (i == 0) ? now : LocalDateTime.of(day, LocalTime.MAX);

            Double kwh = readingRepository.sumEnergyBetween(dayStart, dayEnd);
            Double avgP = readingRepository.avgPowerSince(dayStart);
            BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());

            dailyList.add(new EnergyAnalyticsDto.TimePointData(
                    day.format(dtf),
                    Math.round(kwh * 100.0) / 100.0,
                    Math.round(avgP * 10.0) / 10.0,
                    cost,
                    80.0
            ));
        }
        dto.setDailyTrends(dailyList);

        // Zone breakdown
        List<Object[]> zoneSums = readingRepository.sumEnergyGroupedByZone(startDate, now);
        double totalZoneKwh = Math.max(0.001, totalEnergy);
        List<EnergyAnalyticsDto.ZoneAnalyticsData> zoneList = new ArrayList<>();
        for (Object[] row : zoneSums) {
            String zName = (String) row[1];
            Double kwh = ((Number) row[2]).doubleValue();
            double pct = Math.round((kwh / totalZoneKwh) * 1000.0) / 10.0;
            BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());
            zoneList.add(new EnergyAnalyticsDto.ZoneAnalyticsData(zName, Math.round(kwh * 100.0) / 100.0, pct, cost, 100.0));
        }
        dto.setZoneBreakdown(zoneList);

        // Device breakdown
        List<Object[]> devSums = readingRepository.sumEnergyGroupedByDevice(startDate, now);
        List<EnergyAnalyticsDto.DeviceAnalyticsData> devList = new ArrayList<>();
        for (Object[] row : devSums) {
            String dName = (String) row[1];
            String dIdentifier = (String) row[2];
            Double kwh = ((Number) row[3]).doubleValue();
            double pct = Math.round((kwh / totalZoneKwh) * 1000.0) / 10.0;
            devList.add(new EnergyAnalyticsDto.DeviceAnalyticsData(dName, dIdentifier, Math.round(kwh * 100.0) / 100.0, pct));
        }
        dto.setDeviceBreakdown(devList);

        return dto;
    }
}

