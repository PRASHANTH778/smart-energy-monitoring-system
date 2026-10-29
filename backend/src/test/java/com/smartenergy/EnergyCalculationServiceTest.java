package com.smartenergy;

import com.smartenergy.repository.EnergyTariffRepository;
import com.smartenergy.service.EnergyCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EnergyCalculationServiceTest {

    private EnergyTariffRepository tariffRepository;
    private EnergyCalculationService calculationService;

    @BeforeEach
    void setUp() {
        tariffRepository = Mockito.mock(EnergyTariffRepository.class);
        calculationService = new EnergyCalculationService(tariffRepository);
    }

    @Test
    @DisplayName("Should accurately calculate energy using trapezoidal numerical integration")
    void testTrapezoidalEnergyIntegration() {
        // Average power of (1000W + 2000W) / 2 = 1500W for 36 seconds = 0.01 hours
        // Expected Energy = (1500 * 0.01) / 1000 = 0.015 kWh
        LocalDateTime t1 = LocalDateTime.of(2026, 10, 13, 10, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 10, 13, 10, 0, 36);

        Double energyKwh = calculationService.calculateIntervalEnergy(1000.0, 2000.0, t1, t2);

        assertEquals(0.015, energyKwh, 0.0001);
    }

    @Test
    @DisplayName("Should return 0 energy for duplicate timestamp readings")
    void testDuplicateReadingIntegration() {
        LocalDateTime t1 = LocalDateTime.of(2026, 10, 13, 10, 0, 0);

        Double energyKwh = calculationService.calculateIntervalEnergy(1000.0, 1000.0, t1, t1);

        assertEquals(0.0, energyKwh);
    }

    @Test
    @DisplayName("Should accurately compute electricity cost with BigDecimal rounding")
    void testCostCalculation() {
        Double energyKwh = 120.50;
        BigDecimal rate = new BigDecimal("0.1650");

        // 120.50 * 0.1650 = 19.8825 -> rounded to 19.88
        BigDecimal cost = calculationService.calculateCost(energyKwh, rate);

        assertEquals(new BigDecimal("19.88"), cost);
    }

    @Test
    @DisplayName("Should calculate baseline savings when consumption is under target")
    void testSavingsCalculation() {
        Double actualKwh = 450.0;
        Double baselineKwh = 600.0;
        BigDecimal rate = new BigDecimal("0.1500");

        // (600 - 450) * 0.15 = 150 * 0.15 = 22.50
        BigDecimal savings = calculationService.calculateSavings(actualKwh, baselineKwh, rate);

        assertEquals(new BigDecimal("22.50"), savings);
    }
}

