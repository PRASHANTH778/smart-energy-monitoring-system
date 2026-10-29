package com.smartenergy.service;

import com.smartenergy.entity.EnergyTariff;
import com.smartenergy.repository.EnergyTariffRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Energy Calculation Service
 *
 * Implements numerical time-based integration for energy consumption:
 * E (kWh) = Integral of P(t) dt / 1000
 *
 * Numerical Integration: Trapezoidal Rule
 * Delta E (kWh) = ((P_prev + P_curr) / 2.0) * (Delta_t_seconds / 3600.0) / 1000.0
 *
 * Cost Calculations:
 * Estimated Cost = Energy (kWh) * Tariff Rate per kWh
 * Uses BigDecimal with HALF_UP rounding for financial precision.
 */
@Service
public class EnergyCalculationService {

    private final EnergyTariffRepository tariffRepository;

    public EnergyCalculationService(EnergyTariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    /**
     * Calculates energy in kWh consumed between two successive discrete power readings
     * using the Trapezoidal numerical integration rule.
     *
     * @param prevPowerWatts Power at previous sample
     * @param currPowerWatts Power at current sample
     * @param prevTime Timestamp of previous sample
     * @param currTime Timestamp of current sample
     * @return Energy consumed in kWh during the interval
     */
    public Double calculateIntervalEnergy(Double prevPowerWatts, Double currPowerWatts,
                                          LocalDateTime prevTime, LocalDateTime currTime) {
        if (currPowerWatts == null || currPowerWatts <= 0) {
            return 0.0;
        }

        // Duplicate reading at the exact same timestamp
        if (prevTime != null && currTime != null && currTime.isEqual(prevTime)) {
            return 0.0;
        }

        // If no previous reading, fallback to discrete 5-second equivalent slice
        if (prevTime == null || currTime == null) {
            return (currPowerWatts * (5.0 / 3600.0)) / 1000.0;
        }

        // Ignore out-of-order timestamps
        if (currTime.isBefore(prevTime)) {
            return 0.0;
        }

        long secondsElapsed = Duration.between(prevTime, currTime).getSeconds();

        // Guard against duplicate readings (0 seconds)
        if (secondsElapsed <= 0) {
            return 0.0;
        }

        // Guard against extended offline periods (e.g. device disconnected for days):
        // Cap the integration window to maximum 60 seconds to avoid false energy spikes
        if (secondsElapsed > 60) {
            secondsElapsed = 5;
        }

        double pPrev = (prevPowerWatts != null && prevPowerWatts >= 0) ? prevPowerWatts : currPowerWatts;
        double averagePowerWatts = (pPrev + currPowerWatts) / 2.0;

        double hoursElapsed = secondsElapsed / 3600.0;
        double intervalKwh = (averagePowerWatts * hoursElapsed) / 1000.0;

        return Math.max(0.0, intervalKwh);
    }

    /**
     * Calculates estimated electricity cost using BigDecimal financial rounding
     */
    public BigDecimal calculateCost(Double energyKwh, BigDecimal ratePerKwh) {
        if (energyKwh == null || energyKwh <= 0 || ratePerKwh == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(energyKwh)
                .multiply(ratePerKwh)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates estimated savings against baseline consumption target
     */
    public BigDecimal calculateSavings(Double actualKwh, Double baselineTargetKwh, BigDecimal ratePerKwh) {
        if (actualKwh == null || baselineTargetKwh == null || ratePerKwh == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        double savedKwh = baselineTargetKwh - actualKwh;
        if (savedKwh <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(savedKwh)
                .multiply(ratePerKwh)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Resolves currently active tariff or default fallback
     */
    public EnergyTariff getEffectiveTariff() {
        return tariffRepository.findActiveTariffForDate(LocalDate.now())
                .or(() -> tariffRepository.findFirstByActiveTrueOrderByEffectiveFromDesc())
                .orElseGet(() -> new EnergyTariff("Standard Commercial Tariff", new BigDecimal("0.1500"),
                        LocalDate.now(), null, 500.0, "$"));
    }
}

