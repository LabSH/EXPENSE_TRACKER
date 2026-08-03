package com.expenseTracker.income.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(
        Long incomeId,
        String incomeTypeCd,
        String incomeTypeNm,
        LocalDate incomeDt,
        BigDecimal amount,
        String content,
        String memo
) {}
