package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Rule;
import com.portfoliomanager.entity.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    Optional<Rule> findByRuleId(String ruleId);

    List<Rule> findByActiveTrue();

    List<Rule> findByRuleType(RuleType ruleType);

    List<Rule> findByRuleTypeAndActiveTrue(RuleType ruleType);

    boolean existsByRuleId(String ruleId);
}
