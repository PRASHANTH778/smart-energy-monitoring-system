package com.smartenergy.repository;

import com.smartenergy.entity.AutomationExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, Long> {
    List<AutomationExecution> findTop20ByOrderByExecutedAtDesc();
    List<AutomationExecution> findByRuleIdOrderByExecutedAtDesc(Long ruleId);
}

