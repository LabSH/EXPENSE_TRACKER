package com.expenseTracker.fixedexpense.dto;

import java.util.List;

/** 고정지출 화면 우측 패널 데이터 (요약 + 다가오는 지출 목록) */
public record FixedExpenseUpcomingResponse(
        FixedExpenseSummaryResponse summary,
        List<FixedExpenseUpcomingItemResponse> upcoming
) {}
