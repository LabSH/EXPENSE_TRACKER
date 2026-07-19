package com.expenseTracker.user.dto;

public record UserResponse(
        String userId,
        String loginId,
        String userNm,
        String email,
        String roleCd,
        String useAt
) {}
