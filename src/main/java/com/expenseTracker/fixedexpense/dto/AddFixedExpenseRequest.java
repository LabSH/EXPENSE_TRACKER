package com.expenseTracker.fixedexpense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 고정지출 등록/수정 요청 */
public record AddFixedExpenseRequest(
        @Size(max = 30, message = "결제수단 코드가 올바르지 않습니다.")
        String paymentMethodCd,

        @Size(max = 30, message = "지출주기 코드가 올바르지 않습니다.")
        String expenseCycleCd,

        @Size(max = 30, message = "카테고리 코드가 올바르지 않습니다.")
        String categoryCd,

        // 지출주기 계산 기준일(시작일)
        @NotNull(message = "기준일(시작일)은 필수입니다.")
        LocalDate anchorDt,

        String fileGroupId,

        @NotNull(message = "금액은 필수입니다.")
        BigDecimal amount,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 200, message = "내용은 200자를 넘을 수 없습니다.")
        String content,

        @Size(max = 500, message = "메모는 500자를 넘을 수 없습니다.")
        String memo
) {}
