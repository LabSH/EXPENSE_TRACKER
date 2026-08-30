package com.expenseTracker.fixedexpense.facade;

import com.expenseTracker.attachfile.service.AttachFileService;
import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.service.CodeService;
import com.expenseTracker.fixedexpense.dto.AddFixedExpenseRequest;
import com.expenseTracker.fixedexpense.dto.FixedExpenseResponse;
import com.expenseTracker.fixedexpense.dto.FixedExpenseUpcomingResponse;
import com.expenseTracker.fixedexpense.service.FixedExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 고정지출 화면에서 필요한 고정지출·공통코드 서비스 호출을 조합한다. */
@Service
@RequiredArgsConstructor
public class FixedExpenseFacade {

    private final FixedExpenseService fixedExpenseService;
    private final CodeService codeService;
    private final AttachFileService attachFileService;

    /** 로그인한 사용자의 고정지출 전체 목록 조회 */
    public List<FixedExpenseResponse> findMyFixedExpenses(String loginId) {
        return fixedExpenseService.findMyFixedExpenses(loginId);
    }

    /**
     * 고정지출을 논리 삭제하고 딸린 첨부파일도 함께 논리 삭제한다.
     * 고정지출만 지워지고 첨부파일이 남는 일이 없도록 한 트랜잭션으로 묶는다.
     */
    @Transactional
    public void deleteFixedExpense(String loginId, Long fixedExpenseId) {
        String fileGroupId = fixedExpenseService.deleteFixedExpense(loginId, fixedExpenseId);
        if (fileGroupId != null && !fileGroupId.isBlank()) {
            attachFileService.deleteByGroup(fileGroupId);
        }
    }

    /** 고정지출 화면 우측 패널 데이터(이번 달 요약 + 다가오는 지출 목록) 조회 */
    public FixedExpenseUpcomingResponse findUpcoming(String loginId) {
        return fixedExpenseService.findUpcoming(loginId);
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
