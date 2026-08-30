package com.expenseTracker.fixedexpense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 고정지출 항목별 다음 지출 예정 1건 */
public record FixedExpenseUpcomingItemResponse(
        LocalDate nextDueDt,
        String content,
        String expenseCycleNm,
        BigDecimal amount
) {}
