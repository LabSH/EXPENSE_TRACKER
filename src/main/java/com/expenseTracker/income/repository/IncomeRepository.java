package com.expenseTracker.income.repository;

import com.expenseTracker.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    /** 사용자의 삭제되지 않은 수입을 소득일자 역순으로 조회 */
    List<Income> findByUserIdAndDelAtOrderByIncomeDtDescIncomeIdDesc(String userId, String delAt);
}
