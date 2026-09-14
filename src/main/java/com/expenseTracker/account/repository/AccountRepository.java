package com.expenseTracker.account.repository;

import com.expenseTracker.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    /** 사용자의 삭제되지 않은 계좌를 등록일 역순으로 조회 */
    List<Account> findByUserIdAndDelAtOrderByRgsDtDesc(String userId, String delAt);

    /** 해당 계좌가 사용자 소유이면서 삭제되지 않았는지 확인 */
    boolean existsByAccountIdAndUserIdAndDelAt(Long accountId, String userId, String delAt);
}
