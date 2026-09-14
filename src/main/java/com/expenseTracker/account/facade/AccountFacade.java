package com.expenseTracker.account.facade;

import com.expenseTracker.account.dto.AccountResponse;
import com.expenseTracker.account.dto.AddAccountRequest;
import com.expenseTracker.account.service.AccountService;
import com.expenseTracker.common.exception.BusinessException;
import com.expenseTracker.fixedexpense.service.FixedExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 계좌·카드 화면에서 필요한 계좌 서비스 호출과 도메인 간 조합을 담당한다. */
@Service
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountService accountService;
    private final FixedExpenseService fixedExpenseService;

    /** 로그인한 사용자의 계좌·카드 목록 조회 */
    public List<AccountResponse> findMyAccounts(String loginId) {
        return accountService.findMyAccounts(loginId);
    }

    /** 계좌·카드 등록 */
    public AccountResponse addAccount(String loginId, AddAccountRequest req) {
        return accountService.addAccount(loginId, req.accountTypeCd(), req.accountNm(), req.issuerNm(), req.memo());
    }

    /** 계좌·카드 수정 */
    public AccountResponse updateAccount(String loginId, Long accountId, AddAccountRequest req) {
        return accountService.updateAccount(loginId, accountId, req.accountTypeCd(), req.accountNm(), req.issuerNm(), req.memo());
    }

    /**
     * 계좌·카드 삭제. 이 계좌를 사용 중인 고정지출이 있으면 삭제를 막는다.
     * ACCOUNT_ID 를 조용히 비우면 "어느 계좌에서 나가는 돈인지"라는 사용자 입력이 사라지므로,
     * 사용자가 고정지출에서 계좌를 먼저 바꾸도록 유도한다.
     */
    @Transactional
    public void deleteAccount(String loginId, Long accountId) {
        if (fixedExpenseService.isAccountInUse(accountId)) {
            throw new BusinessException("이 계좌를 사용 중인 고정지출이 있어 삭제할 수 없습니다.");
        }
        accountService.deleteAccount(loginId, accountId);
    }
}
