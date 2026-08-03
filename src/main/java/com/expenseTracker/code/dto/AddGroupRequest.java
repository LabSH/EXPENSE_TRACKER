package com.expenseTracker.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 공통코드 그룹 등록 요청 */
public record AddGroupRequest(
        @NotBlank(message = "그룹ID는 필수입니다.")
        @Size(max = 30, message = "그룹ID는 30자를 넘을 수 없습니다.")
        String groupId,

        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 200, message = "그룹명은 200자를 넘을 수 없습니다.")
        String groupNm
) {}
