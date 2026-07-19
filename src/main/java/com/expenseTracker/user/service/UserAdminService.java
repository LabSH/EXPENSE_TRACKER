package com.expenseTracker.user.service;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private static final int MAX_RESULTS = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAllUsers() {
        Pageable limit = PageRequest.of(0, MAX_RESULTS);
        return userRepository.findByDelAtOrderByRgsDtDesc("N", limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UserResponse> searchUsers(String keyword) {
        Pageable limit = PageRequest.of(0, MAX_RESULTS);
        return userRepository.findByDelAtAndUserNmContainingIgnoreCaseOrderByRgsDtDesc("N", keyword, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserStats getStats() {
        long total = userRepository.countByDelAt("N");
        long active = userRepository.countByDelAtAndUseAt("N", "Y");
        return new UserStats(total, active, total - active);
    }

    @Transactional
    public UserResponse addUser(String loginId, String passwd, String userNm, String email, String roleCd, String adminId) {
        if (userRepository.findByLoginIdAndDelAt(loginId, "N").isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        User user = User.builder()
                .loginId(loginId)
                .passwd(passwordEncoder.encode(passwd))
                .userNm(userNm)
                .email(email == null || email.isBlank() ? null : email)
                .roleCd(roleCd)
                .useAt("Y")
                .delAt("N")
                .rgsDt(LocalDateTime.now())
                .rgsUserId(adminId)
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse toggleStatus(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setUseAt("Y".equals(user.getUseAt()) ? "N" : "Y");
        user.setUpdDt(LocalDateTime.now());
        user.setUpdUserId(adminId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse changeRole(String userId, String roleCd, String adminId) {
        User user = findActiveUser(userId);
        user.setRoleCd(roleCd);
        user.setUpdDt(LocalDateTime.now());
        user.setUpdUserId(adminId);
        return toResponse(user);
    }

    @Transactional
    public void resetPassword(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setPasswd(passwordEncoder.encode(user.getLoginId()));
        user.setUpdDt(LocalDateTime.now());
        user.setUpdUserId(adminId);
    }

    @Transactional
    public void deleteUser(String userId, String adminId) {
        User user = findActiveUser(userId);
        user.setDelAt("Y");
        user.setUpdDt(LocalDateTime.now());
        user.setUpdUserId(adminId);
    }

    private User findActiveUser(String userId) {
        return userRepository.findById(userId)
                .filter(u -> "N".equals(u.getDelAt()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getUserId(), u.getLoginId(), u.getUserNm(), u.getEmail(), u.getRoleCd(), u.getUseAt());
    }
}
