package com.expenseTracker.user.dto;

import jakarta.validation.constraints.NotBlank;

/** 관리자의 사용자 권한 변경 요청 */
public record ChangeRoleRequest(
        @NotBlank(message = "권한은 필수입니다.")
        String roleCd
) {}
