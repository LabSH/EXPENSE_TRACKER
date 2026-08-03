package com.expenseTracker.user.service;

import com.expenseTracker.user.entity.ConsentType;
import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.entity.UserConsent;
import com.expenseTracker.user.model.UserJoinForm;
import com.expenseTracker.user.repository.UserConsentRepository;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final PasswordEncoder passwordEncoder;

    /** 로그인ID 중복 여부 확인 */
    @Transactional(readOnly = true)
    public boolean isLoginIdDuplicate(String loginId) {
        return userRepository.findByLoginIdAndDelAt(loginId, "N").isPresent();
    }

    /** 회원가입 처리 — 비밀번호 암호화 후 저장하고 동의이력 기록 */
    @Transactional
    public void join(UserJoinForm form) {
        if (isLoginIdDuplicate(form.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .loginId(form.getLoginId())
                .passwd(passwordEncoder.encode(form.getPasswd()))
                .userNm(form.getUserNm())
                .nickname(form.getNickname() == null || form.getNickname().isBlank() ? null : form.getNickname())
                .email(form.getEmail() == null || form.getEmail().isBlank() ? null : form.getEmail())
                .roleCd("ROLE_USER")
                .useAt("Y")
                .delAt("N")
                .rgsUserId(form.getLoginId())
                .build();
        userRepository.save(user);

        OffsetDateTime agreedAt = OffsetDateTime.now();
        List<UserConsent> consents = List.of(
                buildConsent(user.getUserId(), ConsentType.TERMS, "Y", agreedAt),
                buildConsent(user.getUserId(), ConsentType.PRIVACY, "Y", agreedAt),
                buildConsent(user.getUserId(), ConsentType.MARKETING, form.isMarketingAgreed() ? "Y" : "N", agreedAt)
        );
        userConsentRepository.saveAll(consents);
    }

    /** 동의이력 엔티티 생성 */
    private UserConsent buildConsent(String userId, ConsentType type, String agreed, OffsetDateTime agreedAt) {
        return UserConsent.builder()
                .userId(userId)
                .consentType(type)
                .agreed(agreed)
                .agreedAt(agreedAt)
                .build();
    }
}
