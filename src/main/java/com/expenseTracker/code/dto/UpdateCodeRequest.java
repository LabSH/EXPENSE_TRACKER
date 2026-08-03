package com.expenseTracker.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 공통코드 항목 수정 요청 */
public record UpdateCodeRequest(
        @NotBlank(message = "코드명은 필수입니다.")
        @Size(max = 200, message = "코드명은 200자를 넘을 수 없습니다.")
        String codeNm,

        int sortSn,

        @NotBlank(message = "사용여부는 필수입니다.")
        String useAt
) {}
