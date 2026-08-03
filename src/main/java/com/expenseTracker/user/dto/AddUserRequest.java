package com.expenseTracker.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 관리자의 사용자 등록 요청 */
public record AddUserRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "아이디는 영문, 숫자만 사용해 4~20자로 입력해주세요.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String passwd,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 100)
        String userNm,

        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 200)
        String email,

        @NotBlank(message = "권한은 필수입니다.")
        String roleCd
) {}
