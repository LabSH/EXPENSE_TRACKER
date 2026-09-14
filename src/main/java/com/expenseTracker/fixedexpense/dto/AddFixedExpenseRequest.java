package com.expenseTracker.fixedexpense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 고정지출 등록/수정 요청 */
public record AddFixedExpenseRequest(
        // 어느 계좌·카드에서 나가는지(선택). 본인 소유 여부는 Service에서 검증
        Long accountId,

        // 자동이체여부. Y=자동이체, N=수동이체. null/빈 값이면 Service에서 'N' 처리
        @Pattern(regexp = "^$|^[YN]$", message = "자동이체 여부가 올바르지 않습니다.")
        String autoPayAt,

        @Size(max = 30, message = "지출주기 코드가 올바르지 않습니다.")
        String expenseCycleCd,

        @Size(max = 30, message = "카테고리 코드가 올바르지 않습니다.")
        String categoryCd,

        // 지출주기 계산 기준일(시작일)
        @NotNull(message = "기준일(시작일)은 필수입니다.")
        LocalDate anchorDt,

        // 종료일(선택). null이면 무기한. anchorDt 이상인지는 Service에서 검증
        LocalDate endDt,

        String fileGroupId,

        @NotNull(message = "금액은 필수입니다.")
        BigDecimal amount,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 200, message = "내용은 200자를 넘을 수 없습니다.")
        String content,

        @Size(max = 500, message = "메모는 500자를 넘을 수 없습니다.")
        String memo
) {}
