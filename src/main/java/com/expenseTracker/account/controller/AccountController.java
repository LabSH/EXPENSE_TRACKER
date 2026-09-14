package com.expenseTracker.account.controller;

import com.expenseTracker.account.dto.AccountResponse;
import com.expenseTracker.account.dto.AddAccountRequest;
import com.expenseTracker.account.facade.AccountFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 계좌·카드 관리 API.
 * 전용 페이지 없이 설정 화면(setting/index)의 카드에서 호출하는 JSON 엔드포인트만 제공한다.
 * 계좌유형 옵션은 CodeModelAdvice 가 주입하는 codes['ACCOUNTTYPE'] 를 화면에서 그대로 쓴다.
 */
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountFacade accountFacade;

    /** 로그인한 사용자의 계좌·카드 목록 조회 */
    @GetMapping("/account/data")
    public List<AccountResponse> list(Authentication auth) {
        return accountFacade.findMyAccounts(auth.getName());
    }

    /** 계좌·카드 등록 */
    @PostMapping("/account")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse add(@Valid @RequestBody AddAccountRequest req, Authentication auth) {
        return accountFacade.addAccount(auth.getName(), req);
    }

    /** 계좌·카드 수정 */
    @PutMapping("/account/{id}")
    public AccountResponse update(@PathVariable Long id, @Valid @RequestBody AddAccountRequest req, Authentication auth) {
        return accountFacade.updateAccount(auth.getName(), id, req);
    }

    /** 계좌·카드 삭제 */
    @DeleteMapping("/account/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        accountFacade.deleteAccount(auth.getName(), id);
    }
}
