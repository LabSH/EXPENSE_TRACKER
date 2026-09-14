package com.expenseTracker.account.service;

import com.expenseTracker.account.dto.AccountResponse;
import com.expenseTracker.account.entity.Account;
import com.expenseTracker.account.repository.AccountRepository;
import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.common.exception.BusinessException;
import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String ACCOUNT_TYPE_GROUP_ID = "ACCOUNTTYPE";

    private final AccountRepository accountRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    /** 로그인한 사용자의 계좌·카드 목록 조회 */
    @Transactional(readOnly = true)
    public List<AccountResponse> findMyAccounts(String loginId) {
        User user = findActiveUser(loginId);
        return toResponses(accountRepository.findByUserIdAndDelAtOrderByRgsDtDesc(user.getUserId(), "N"));
    }

    /**
     * 계좌·카드를 공통 드롭다운 fragment 용 옵션으로 변환한다.
     * codeId = 계좌ID 문자열, codeNm = "생활비 카드 (국민)" 형태의 표시 라벨.
     */
    @Transactional(readOnly = true)
    public List<CodeResponse> findMyAccountOptions(String loginId) {
        return findMyAccounts(loginId).stream()
                .map(a -> new CodeResponse(String.valueOf(a.accountId()), "ACCOUNT", buildOptionLabel(a), 0, "Y"))
                .toList();
    }

    /** "생활비 카드 (국민)" — 발급기관이 있으면 괄호로 덧붙인다 */
    private String buildOptionLabel(AccountResponse a) {
        return a.issuerNm() != null ? a.accountNm() + " (" + a.issuerNm() + ")" : a.accountNm();
    }

    /** 계좌·카드 등록 */
    @Transactional
    public AccountResponse addAccount(String loginId, String accountTypeCd, String accountNm, String issuerNm, String memo) {
        User user = findActiveUser(loginId);

        Account account = Account.builder()
                .userId(user.getUserId())
                .accountTypeCd(accountTypeCd)
                .accountNm(accountNm)
                .issuerNm(blankToNull(issuerNm))
                .memo(blankToNull(memo))
                .useAt("Y")
                .delAt("N")
                .rgsUserId(loginId)
                .build();
        accountRepository.save(account);

        return toResponses(List.of(account)).getFirst();
    }

    /** 계좌·카드 수정. 본인 소유이면서 삭제되지 않은 건만 수정 가능 */
    @Transactional
    public AccountResponse updateAccount(String loginId, Long accountId, String accountTypeCd, String accountNm, String issuerNm, String memo) {
        User user = findActiveUser(loginId);
        Account account = findOwnedAccount(user.getUserId(), accountId);

        account.setAccountTypeCd(accountTypeCd);
        account.setAccountNm(accountNm);
        account.setIssuerNm(blankToNull(issuerNm));
        account.setMemo(blankToNull(memo));
        account.setUpdUserId(loginId);

        return toResponses(List.of(account)).getFirst();
    }

    /** 계좌·카드 논리 삭제. 본인 소유이면서 삭제되지 않은 건만 삭제 가능 */
    @Transactional
    public void deleteAccount(String loginId, Long accountId) {
        User user = findActiveUser(loginId);
        Account account = findOwnedAccount(user.getUserId(), accountId);

        account.setDelAt("Y");
        account.setUpdUserId(loginId);
    }

    /** 삭제되지 않은 사용자를 로그인ID로 조회, 없으면 예외 발생 */
    private User findActiveUser(String loginId) {
        return userRepository.findByLoginIdAndDelAt(loginId, "N")
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));
    }

    /** 본인 소유이면서 삭제되지 않은 계좌 조회, 없으면 예외 발생 */
    private Account findOwnedAccount(String userId, Long accountId) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUserId().equals(userId) && "N".equals(a.getDelAt()))
                .orElseThrow(() -> new BusinessException("계좌를 찾을 수 없습니다."));
    }

    /** Account 엔티티 목록을 계좌유형명을 채운 응답 DTO 목록으로 변환 */
    private List<AccountResponse> toResponses(List<Account> accounts) {
        Map<String, String> accountTypeNames = codeRepository.findByIdGroupId(ACCOUNT_TYPE_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));

        return accounts.stream()
                .map(a -> new AccountResponse(
                        a.getAccountId(),
                        a.getAccountTypeCd(),
                        accountTypeNames.getOrDefault(a.getAccountTypeCd(), a.getAccountTypeCd()),
                        a.getAccountNm(),
                        a.getIssuerNm(),
                        a.getMemo(),
                        a.getRgsDt()))
                .toList();
    }

    /** 빈 문자열은 NULL 로 저장한다 (선택 입력값) */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
