package com.expenseTracker.user.repository;

import com.expenseTracker.user.entity.ConsentType;
import com.expenseTracker.user.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConsentRepository extends JpaRepository<UserConsent, String> {
    /** 사용자ID와 동의유형으로 가장 최근 동의이력 조회 (현재 동의 상태 확인용) */
    Optional<UserConsent> findTopByUserIdAndConsentTypeOrderByAgreedAtDesc(String userId, ConsentType consentType);
}
