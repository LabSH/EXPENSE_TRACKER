package com.expenseTracker.account.dto;

import java.time.LocalDateTime;

/** 계좌·카드 1건 응답. 화면의 아이콘·색상은 accountTypeCd 로 결정한다 */
public record AccountResponse(
        Long accountId,
        String accountTypeCd,
        String accountTypeNm,
        String accountNm,
        String issuerNm,
        String memo,
        LocalDateTime rgsDt
) {}
