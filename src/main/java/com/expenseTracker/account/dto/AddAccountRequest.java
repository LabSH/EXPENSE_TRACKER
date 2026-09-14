package com.expenseTracker.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 계좌·카드 등록/수정 요청 */
public record AddAccountRequest(
        @NotBlank(message = "계좌유형은 필수입니다.")
        @Size(max = 30, message = "계좌유형 코드가 올바르지 않습니다.")
        String accountTypeCd,

        @NotBlank(message = "계좌명은 필수입니다.")
        @Size(max = 50, message = "계좌명은 50자를 넘을 수 없습니다.")
        String accountNm,

        @Size(max = 50, message = "발급기관명은 50자를 넘을 수 없습니다.")
        String issuerNm,

        // 참고용 메모(선택). 민감정보는 입력하지 않도록 화면에서 안내한다
        @Size(max = 200, message = "메모는 200자를 넘을 수 없습니다.")
        String memo
) {}
