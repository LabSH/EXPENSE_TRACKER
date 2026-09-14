package com.expenseTracker.fixedexpense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 고정지출 항목별 다음 지출 예정 1건 */
public record FixedExpenseUpcomingItemResponse(
        LocalDate nextDueDt,
        String content,
        String expenseCycleNm,
        BigDecimal amount,
        // 자동이체여부. 수동이체(N)는 우측 패널에서 "직접 이체할 건"으로 강조 표시된다
        String autoPayAt,
        // 회차. 월 기반 주기(매월~매년)에서만 채워지고 그 외에는 null
        Integer installmentNo,
        // 총 회차. 종료일이 없거나 회차 표시 대상이 아니면 null
        Integer totalInstallments
) {}
