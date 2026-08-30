package com.expenseTracker.fixedexpense.dto;

import java.math.BigDecimal;

/**
 * 고정지출 화면 우측 패널의 요약 지표.
 * 화면의 "이번 달 준비 금액"은 monthlyTotal + monthlyReserve 로 프론트에서 계산한다.
 */
public record FixedExpenseSummaryResponse(
        BigDecimal monthlyTotal,
        BigDecimal monthlyReserve,
        BigDecimal remaining
) {}
