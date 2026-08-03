package com.expenseTracker.user.repository;

import com.expenseTracker.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    /** 로그인ID와 삭제여부로 사용자 조회 */
    Optional<User> findByLoginIdAndDelAt(String loginId, String delAt);
    /** 삭제여부로 사용자 목록을 등록일 역순으로 조회 */
    List<User> findByDelAtOrderByRgsDtDesc(String delAt, Pageable pageable);
    /** 삭제여부와 이름 키워드로 사용자 목록을 등록일 역순으로 조회 */
    List<User> findByDelAtAndUserNmContainingIgnoreCaseOrderByRgsDtDesc(String delAt, String keyword, Pageable pageable);
    /** 삭제여부로 사용자 수 집계 */
    long countByDelAt(String delAt);
    /** 삭제여부와 사용여부로 사용자 수 집계 */
    long countByDelAtAndUseAt(String delAt, String useAt);
}
