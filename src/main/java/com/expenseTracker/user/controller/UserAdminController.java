package com.expenseTracker.user.controller;

import com.expenseTracker.user.dto.AddUserRequest;
import com.expenseTracker.user.dto.ChangeRoleRequest;
import com.expenseTracker.user.dto.UserResponse;
import com.expenseTracker.user.dto.UserStats;
import com.expenseTracker.user.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    /** 키워드로 사용자 검색 (키워드 없으면 전체 목록) */
    @GetMapping("/data")
    public List<UserResponse> search(@RequestParam(defaultValue = "") String keyword) {
        return keyword.isBlank() ? userAdminService.findAllUsers() : userAdminService.searchUsers(keyword);
    }

    /** 사용자 통계(전체/활성/비활성) 조회 */
    @GetMapping("/stats")
    public UserStats stats() {
        return userAdminService.getStats();
    }

    /** 관리자가 사용자를 신규 등록 */
    @PostMapping
    public ResponseEntity<UserResponse> add(@Valid @RequestBody AddUserRequest req,
                                             @AuthenticationPrincipal UserDetails principal) {
        UserResponse added = userAdminService.addUser(
                req.loginId(), req.passwd(), req.userNm(), req.email(), req.roleCd(), principal.getUsername());
        return ResponseEntity.ok(added);
    }

    /** 사용자 활성/비활성 상태 토글 */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable String userId,
                                                      @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdminService.updateStatus(userId, principal.getUsername()));
    }

    /** 사용자 권한 변경 */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable String userId,
                                                    @Valid @RequestBody ChangeRoleRequest req,
                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdminService.updateRole(userId, req.roleCd(), principal.getUsername()));
    }

    /** 사용자 비밀번호를 초기화(로그인ID로 재설정) */
    @PostMapping("/{userId}/password-reset")
    public ResponseEntity<Void> resetPassword(@PathVariable String userId,
                                               @AuthenticationPrincipal UserDetails principal) {
        userAdminService.updatePasswordToDefault(userId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** 사용자 삭제(논리 삭제) */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId,
                                            @AuthenticationPrincipal UserDetails principal) {
        userAdminService.deleteUser(userId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
