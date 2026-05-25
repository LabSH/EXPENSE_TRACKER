package com.expenseTracker.user.repository;

import com.expenseTracker.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    /** 로그인ID와 삭제여부로 활성 사용자 조회 */
    Optional<User> findByLoginIdAndDelAt(String loginId, String delAt);
}
