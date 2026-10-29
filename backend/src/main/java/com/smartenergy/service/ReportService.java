package com.smartenergy.service;

import com.smartenergy.dto.ReportResponseDto;
import com.smartenergy.entity.EnergyTariff;
import com.smartenergy.entity.Zone;
import com.smartenergy.repository.AlertRepository;
import com.smartenergy.repository.SensorReadingRepository;
import com.smartenergy.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SensorReadingRepository readingRepository;
    private final ZoneRepository zoneRepository;
    private final AlertRepository alertRepository;
    private final EnergyCalculationService calculationService;

    public ReportService(SensorReadingRepository readingRepository,
                         ZoneRepository zoneRepository,
                         AlertRepository alertRepository,
                         EnergyCalculationService calculationService) {
        this.readingRepository = readingRepository;
        this.zoneRepository = zoneRepository;
        this.alertRepository = alertRepository;
        this.calculationService = calculationService;
    }

    public ReportResponseDto generateReport(String type, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDt = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDt = LocalDateTime.of(endDate, LocalTime.MAX);

        EnergyTariff tariff = calculationService.getEffectiveTariff();

        ReportResponseDto report = new ReportResponseDto();
        report.setReportType(type != null ? type.toUpperCase() : "DAILY");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setAverageTariffRate(tariff.getRatePerKwh());

        Double totalEnergy = readingRepository.sumEnergyBetween(startDt, endDt);
        report.setTotalEnergyKwh(Math.round(totalEnergy * 100.0) / 100.0);
        report.setTotalEstimatedCost(calculationService.calculateCost(totalEnergy, tariff.getRatePerKwh()));

        List<ReportResponseDto.ReportItem> items = new ArrayList<>();
        int violationsCount = 0;

        if ("ZONE".equalsIgnoreCase(type)) {
            List<Object[]> zoneSums = readingRepository.sumEnergyGroupedByZone(startDt, endDt);
            Map<Long, Zone> zoneMap = zoneRepository.findAll().stream()
                    .collect(Collectors.toMap(Zone::getId, z -> z));

            for (Object[] row : zoneSums) {
                Long zoneId = ((Number) row[0]).longValue();
                String zoneName = (String) row[1];
                Double kwh = ((Number) row[2]).doubleValue();
                Double target = zoneMap.containsKey(zoneId) ? zoneMap.get(zoneId).getDailyEnergyTargetKwh() : 100.0;
                BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());
                String status = kwh > target ? "TARGET_EXCEEDED" : "NORMAL";
                if ("TARGET_EXCEEDED".equals(status)) {
                    violationsCount++;
                }

                items.add(new ReportResponseDto.ReportItem(
                        zoneName, Math.round(kwh * 100.0) / 100.0, 0.0, cost, target, status
                ));
            }
        } else if ("DEVICE".equalsIgnoreCase(type)) {
            List<Object[]> devSums = readingRepository.sumEnergyGroupedByDevice(startDt, endDt);
            for (Object[] row : devSums) {
                String devName = (String) row[1];
                String devId = (String) row[2];
                Double kwh = ((Number) row[3]).doubleValue();
                BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());

                items.add(new ReportResponseDto.ReportItem(
                        devName + " (" + devId + ")", Math.round(kwh * 100.0) / 100.0, 0.0, cost, 0.0, "NORMAL"
                ));
            }
        } else {
            // Daily time series report
            LocalDate current = startDate;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)");

            while (!current.isAfter(endDate)) {
                LocalDateTime dayStart = LocalDateTime.of(current, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

                Double kwh = readingRepository.sumEnergyBetween(dayStart, dayEnd);
                Double avgP = readingRepository.avgPowerSince(dayStart);
                BigDecimal cost = calculationService.calculateCost(kwh, tariff.getRatePerKwh());
                Double target = 100.0;
                String status = kwh > target ? "TARGET_EXCEEDED" : "NORMAL";
                if ("TARGET_EXCEEDED".equals(status)) {
                    violationsCount++;
                }

                items.add(new ReportResponseDto.ReportItem(
                        current.format(dtf),
                        Math.round(kwh * 100.0) / 100.0,
                        Math.round(avgP * 10.0) / 10.0,
                        cost,
                        target,
                        status
                ));
                current = current.plusDays(1);
            }
        }

        report.setItems(items);
        report.setTargetViolationsCount(violationsCount);
        report.setTotalAlertsCount((int) alertRepository.count());

        return report;
    }

    public ByteArrayInputStream exportReportToCsv(String type, LocalDate startDate, LocalDate endDate) {
        ReportResponseDto report = generateReport(type, startDate, endDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Smart Building IoT Energy Management Report");
        writer.println("Report Type," + report.getReportType());
        writer.println("Date Range," + report.getStartDate() + " to " + report.getEndDate());
        writer.println("Total Energy (kWh)," + report.getTotalEnergyKwh());
        writer.println("Total Estimated Cost ($)," + report.getTotalEstimatedCost());
        writer.println("Applicable Tariff Rate ($/kWh)," + report.getAverageTariffRate());
        writer.println("Target Violations," + report.getTargetViolationsCount());
        writer.println();
        writer.println("Period/Entity,Energy Consumed (kWh),Avg Power (W),Estimated Cost ($),Target (kWh),Status");

        for (ReportResponseDto.ReportItem item : report.getItems()) {
            writer.printf("\"%s\",%.2f,%.1f,%.2f,%.1f,%s%n",
                    item.getPeriodOrEntity(),
                    item.getEnergyKwh(),
                    item.getAvgPowerWatts(),
                    item.getCost(),
                    item.getTargetKwh(),
                    item.getStatus());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }
}

