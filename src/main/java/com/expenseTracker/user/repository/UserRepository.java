package com.expenseTracker.user.repository;

import com.expenseTracker.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByLoginIdAndDelAt(String loginId, String delAt);
    List<User> findByDelAtOrderByRgsDtDesc(String delAt, Pageable pageable);
    List<User> findByDelAtAndUserNmContainingIgnoreCaseOrderByRgsDtDesc(String delAt, String keyword, Pageable pageable);
    long countByDelAt(String delAt);
    long countByDelAtAndUseAt(String delAt, String useAt);
}
