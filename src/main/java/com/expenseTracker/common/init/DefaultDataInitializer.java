package com.expenseTracker.common.init;

import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DefaultDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 앱 시작 시 초기 관리자 계정이 없으면 자동 생성 */
    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByLoginIdAndDelAt("admin", "N").isEmpty()) {
            User admin = User.builder()
                    .loginId("admin")
                    .passwd(passwordEncoder.encode("admin"))
                    .userNm("관리자")
                    .roleCd("ROLE_ADMIN")
                    .useAt("Y")
                    .delAt("N")
                    .rgsDt(LocalDateTime.now())
                    .rgsUserId("SYSTEM")
                    .build();
            userRepository.save(admin);
        }
    }
}
