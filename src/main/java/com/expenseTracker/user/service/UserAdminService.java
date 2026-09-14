package com.expenseTracker.user.service;

import com.expenseTracker.common.exception.BusinessException;
import com.expenseTracker.user.dto.UserResponse;
import com.expenseTracker.user.dto.UserStats;
import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private static final int MAX_RESULTS = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 삭제되지 않은 사용자 전체 목록을 등록일 역순으로 조회 */
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        Pageable limit = PageRequest.of(0, MAX_RESULTS);
        return userRepository.findByDelAtOrderByRgsDtDesc("N", limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** 이름 키워드로 사용자 검색 */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        Pageable limit = PageRequest.of(0, MAX_RESULTS);
        return userRepository.findByDelAtAndUserNmContainingIgnoreCaseOrderByRgsDtDesc("N", keyword, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** 전체/활성/비활성 사용자 통계 집계 */
    @Transactional(readOnly = true)
    public UserStats getStats() {
        long total = userRepository.countByDelAt("N");
        long active = userRepository.countByDelAtAndUseAt("N", "Y");
        return new UserStats(total, active, total - active);
    }

    /** 관리자가 신규 사용자를 등록. 로그인ID 중복이면 예외 발생 */
    @Transactional
    public UserResponse addUser(String loginId, String passwd, String userNm, String email, String roleCd, String adminId) {
        if (userRepository.findByLoginIdAndDelAt(loginId, "N").isPresent()) {
            throw new BusinessException("이미 사용 중인 아이디입니다.");
        }
        User user = User.builder()
                .loginId(loginId)
                .passwd(passwordEncoder.encode(passwd))
                .userNm(userNm)
                .email(email == null || email.isBlank() ? null : email)
                .roleCd(roleCd)
                .useAt("Y")
                .delAt("N")
                .rgsUserId(adminId)
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    /** 사용자 활성/비활성 상태를 토글 */
    @Transactional
    public UserResponse updateStatus(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setUseAt("Y".equals(user.getUseAt()) ? "N" : "Y");
        user.setUpdUserId(adminId);
        return toResponse(user);
    }

    /** 사용자 권한 변경 */
    @Transactional
    public UserResponse updateRole(String userId, String roleCd, String adminId) {
        User user = findActiveUser(userId);
        user.setRoleCd(roleCd);
        user.setUpdUserId(adminId);
        return toResponse(user);
    }

    /** 사용자 비밀번호를 로그인ID 기반 값으로 초기화 */
    @Transactional
    public void updatePasswordToDefault(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setPasswd(passwordEncoder.encode(user.getLoginId()));
        user.setUpdUserId(adminId);
    }

    /** 사용자 논리 삭제 */
    @Transactional
    public void deleteUser(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setDelAt("Y");
        user.setUpdUserId(adminId);
    }

    /** 삭제되지 않은 사용자를 ID로 조회, 없으면 예외 발생 */
    private User findActiveUser(String userId) {
        return userRepository.findById(userId)
                .filter(u -> "N".equals(u.getDelAt()))
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));
    }

    /** User 엔티티를 응답 DTO로 변환 */
    private UserResponse toResponse(User u) {
        return new UserResponse(u.getUserId(), u.getLoginId(), u.getUserNm(), u.getEmail(), u.getRoleCd(), u.getUseAt());
    }
}
