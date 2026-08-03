package com.expenseTracker.fixedexpense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FixedExpenseResponse(
        Long fixedExpenseId,
        String paymentMethodCd,
        String paymentMethodNm,
        String expenseCycleCd,
        String expenseCycleNm,
        String categoryCd,
        String categoryNm,
        LocalDate anchorDt,
        LocalDate nextDueDt,
        String fileGroupId,
        BigDecimal amount,
        String content,
        String memo,
        LocalDateTime rgsDt
) {}
