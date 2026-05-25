package com.expenseTracker.user.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinForm {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "아이디는 영문, 숫자만 사용해 4~20자로 입력해주세요.")
    private String loginId;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String passwd;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100)
    private String userNm;

    @Size(max = 100)
    private String nickname;

    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 200)
    private String email;

    @AssertTrue(message = "이용약관에 동의해주세요.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 수집·이용에 동의해주세요.")
    private boolean privacyAgreed;

    private boolean marketingAgreed;
}
