package com.expenseTracker.user.controller;

import com.expenseTracker.user.dto.AddUserRequest;
import com.expenseTracker.user.dto.ChangeRoleRequest;
import com.expenseTracker.user.dto.UserResponse;
import com.expenseTracker.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @PostMapping
    public ResponseEntity<UserResponse> add(@RequestBody AddUserRequest req,
                                             @AuthenticationPrincipal UserDetails principal) {
        UserResponse added = userAdminService.addUser(
                req.loginId(), req.passwd(), req.userNm(), req.email(), req.roleCd(), principal.getUsername());
        return ResponseEntity.ok(added);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable String userId,
                                                      @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdminService.toggleStatus(userId, principal.getUsername()));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable String userId,
                                                    @RequestBody ChangeRoleRequest req,
                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userAdminService.changeRole(userId, req.roleCd(), principal.getUsername()));
    }

    @PostMapping("/{userId}/password-reset")
    public ResponseEntity<Void> resetPassword(@PathVariable String userId,
                                               @AuthenticationPrincipal UserDetails principal) {
        userAdminService.resetPassword(userId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId,
                                            @AuthenticationPrincipal UserDetails principal) {
        userAdminService.deleteUser(userId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
