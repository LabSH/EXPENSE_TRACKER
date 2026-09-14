package com.expenseTracker.fixedexpense.repository;

import com.expenseTracker.fixedexpense.entity.FixedExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, Long> {
    /** 사용자의 삭제되지 않은 고정지출을 등록일 역순으로 조회 */
    List<FixedExpense> findByUserIdAndDelAtOrderByRgsDtDesc(String userId, String delAt);

    /** keyword가 빈 문자열이거나 accountId/expenseCycleCd가 비면 해당 조건은 무시하고 검색 */
    @Query("""
            SELECT M1 FROM FixedExpense M1
            WHERE M1.userId = :userId AND M1.delAt = :delAt
            AND (:keyword = '' OR LOWER(M1.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:accountId IS NULL OR M1.accountId = :accountId)
            AND (:expenseCycleCd = '' OR M1.expenseCycleCd = :expenseCycleCd)
            ORDER BY M1.rgsDt DESC
            """)
    List<FixedExpense> search(@Param("userId") String userId, @Param("delAt") String delAt,
                               @Param("keyword") String keyword, @Param("accountId") Long accountId,
                               @Param("expenseCycleCd") String expenseCycleCd);

    /** 해당 계좌를 참조하는(삭제되지 않은) 고정지출 건수 */
    long countByAccountIdAndDelAt(Long accountId, String delAt);
}
