package com.expenseTracker.fixedexpense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FixedExpenseResponse(
        Long fixedExpenseId,
        Long accountId,
        String accountTypeCd,
        String accountNm,
        String autoPayAt,
        String expenseCycleCd,
        String expenseCycleNm,
        String categoryCd,
        String categoryNm,
        LocalDate anchorDt,
        LocalDate endDt,
        LocalDate nextDueDt,
        boolean ended,
        String fileGroupId,
        BigDecimal amount,
        String content,
        String memo,
        LocalDateTime rgsDt
) {}
