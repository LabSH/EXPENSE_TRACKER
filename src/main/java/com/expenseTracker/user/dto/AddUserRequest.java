package com.expenseTracker.user.dto;

public record AddUserRequest(String loginId, String passwd, String userNm, String email, String roleCd) {}
