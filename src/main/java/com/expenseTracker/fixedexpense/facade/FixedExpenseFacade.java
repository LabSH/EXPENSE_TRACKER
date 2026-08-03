package com.expenseTracker.fixedexpense.facade;

import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.service.CodeService;
import com.expenseTracker.fixedexpense.dto.AddFixedExpenseRequest;
import com.expenseTracker.fixedexpense.dto.FixedExpenseResponse;
import com.expenseTracker.fixedexpense.service.FixedExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 고정지출 화면에서 필요한 고정지출·공통코드 서비스 호출을 조합한다. */
@Service
@RequiredArgsConstructor
public class FixedExpenseFacade {

    private final FixedExpenseService fixedExpenseService;
    private final CodeService codeService;

    /** 로그인한 사용자의 고정지출 전체 목록 조회 */
    public List<FixedExpenseResponse> findMyFixedExpenses(String loginId) {
        return fixedExpenseService.findMyFixedExpenses(loginId);
    }

    /** 결제수단 공통코드 목록 조회 */
    public List<CodeResponse> findPaymentMethods() {
        return codeService.findCodesByGroup(FixedExpenseService.PAYMENT_METHOD_GROUP_ID);
    }

    /** 지출주기 공통코드 목록 조회 */
    public List<CodeResponse> findExpenseCycles() {
        return codeService.findCodesByGroup(FixedExpenseService.EXPENSE_CYCLE_GROUP_ID);
    }

    /** 카테고리 공통코드 목록 조회 */
    public List<CodeResponse> findCategories() {
        return codeService.findCodesByGroup(FixedExpenseService.CATEGORY_GROUP_ID);
    }

    /** 키워드/결제수단/지출주기 조건으로 고정지출 검색 */
    public List<FixedExpenseResponse> searchFixedExpenses(String loginId, String keyword,
                                                          String paymentMethodCd, String expenseCycleCd) {
        return fixedExpenseService.searchFixedExpenses(loginId, keyword, paymentMethodCd, expenseCycleCd);
    }

    /** 고정지출 등록 */
    public FixedExpenseResponse addFixedExpense(String loginId, AddFixedExpenseRequest req) {
        return fixedExpenseService.addFixedExpense(loginId, req.paymentMethodCd(), req.expenseCycleCd(),
                req.categoryCd(), req.anchorDt(), req.fileGroupId(), req.amount(), req.content(), req.memo());
    }

    /** 고정지출 수정 */
    public FixedExpenseResponse updateFixedExpense(String loginId, Long fixedExpenseId, AddFixedExpenseRequest req) {
        return fixedExpenseService.updateFixedExpense(loginId, fixedExpenseId, req.paymentMethodCd(),
                req.expenseCycleCd(), req.categoryCd(), req.anchorDt(), req.fileGroupId(), req.amount(), req.content(), req.memo());
    }
}
