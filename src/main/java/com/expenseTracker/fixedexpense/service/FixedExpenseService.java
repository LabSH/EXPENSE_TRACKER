package com.expenseTracker.fixedexpense.service;

import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.fixedexpense.dto.FixedExpenseResponse;
import com.expenseTracker.fixedexpense.entity.FixedExpense;
import com.expenseTracker.fixedexpense.repository.FixedExpenseRepository;
import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixedExpenseService {

    public static final String PAYMENT_METHOD_GROUP_ID = "PAYMENTMETHOD";
    public static final String EXPENSE_CYCLE_GROUP_ID = "EXPENSECYCLE";
    public static final String CATEGORY_GROUP_ID = "CATEGORY";

    // EXPENSECYCLE 공통코드 그룹의 코드ID (DATABASE/DML/TB_CO_CODE_EXPENSECYCLE.sql 참조)
    private static final String CYCLE_WEEKLY = "EXPENSECYCLE_002";
    private static final String CYCLE_BIWEEKLY = "EXPENSECYCLE_003";
    private static final String CYCLE_MONTHLY = "EXPENSECYCLE_004";
    private static final String CYCLE_BIMONTHLY = "EXPENSECYCLE_005";
    private static final String CYCLE_QUARTERLY = "EXPENSECYCLE_006";
    private static final String CYCLE_SEMIANNUAL = "EXPENSECYCLE_007";
    private static final String CYCLE_YEARLY = "EXPENSECYCLE_008";

    private final FixedExpenseRepository fixedExpenseRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    /** 로그인한 사용자의 고정지출 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> findMyFixedExpenses(String loginId) {
        User user = findActiveUser(loginId);
        return toResponses(fixedExpenseRepository.findByUserIdAndDelAtOrderByRgsDtDesc(user.getUserId(), "N"));
    }

    /** 키워드/결제수단/지출주기 조건으로 검색 (빈 값인 조건은 무시) */
    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> searchFixedExpenses(String loginId, String keyword, String paymentMethodCd, String expenseCycleCd) {
        User user = findActiveUser(loginId);
        return toResponses(fixedExpenseRepository.search(user.getUserId(), "N", keyword, paymentMethodCd, expenseCycleCd));
    }

    /** 삭제되지 않은 사용자를 로그인ID로 조회, 없으면 예외 발생 */
    private User findActiveUser(String loginId) {
        return userRepository.findByLoginIdAndDelAt(loginId, "N")
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /** FixedExpense 엔티티 목록을 결제수단·지출주기명을 채운 응답 DTO 목록으로 변환 */
    private List<FixedExpenseResponse> toResponses(List<FixedExpense> fixedExpenses) {
        Map<String, String> paymentMethodNames = codeRepository.findByIdGroupId(PAYMENT_METHOD_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));
        Map<String, String> expenseCycleNames = codeRepository.findByIdGroupId(EXPENSE_CYCLE_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));
        Map<String, String> categoryNames = codeRepository.findByIdGroupId(CATEGORY_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));

        return fixedExpenses.stream()
                .map(fe -> new FixedExpenseResponse(
                        fe.getFixedExpenseId(),
                        fe.getPaymentMethodCd(),
                        paymentMethodNames.getOrDefault(fe.getPaymentMethodCd(), fe.getPaymentMethodCd()),
                        fe.getExpenseCycleCd(),
                        expenseCycleNames.getOrDefault(fe.getExpenseCycleCd(), fe.getExpenseCycleCd()),
                        fe.getCategoryCd(),
                        categoryNames.getOrDefault(fe.getCategoryCd(), fe.getCategoryCd()),
                        fe.getAnchorDt(),
                        calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), LocalDate.now()),
                        fe.getFileGroupId(),
                        fe.getAmount(),
                        fe.getContent(),
                        fe.getMemo(),
                        fe.getRgsDt()))
                .toList();
    }

    /**
     * 기준일과 지출주기로부터 오늘 이후 첫 지출 예정일을 계산.
     * 기준일이 없거나(anchorDt) 매일 주기처럼 기준일 개념이 없는 주기면 null 반환.
     */
    private LocalDate calculateNextDueDate(String expenseCycleCd, LocalDate anchorDt, LocalDate today) {
        if (anchorDt == null || expenseCycleCd == null) return null;

        return switch (expenseCycleCd) {
            case CYCLE_WEEKLY -> resolveByDayInterval(anchorDt, 7, today);
            case CYCLE_BIWEEKLY -> resolveByDayInterval(anchorDt, 14, today);
            case CYCLE_MONTHLY -> resolveByMonthInterval(anchorDt, 1, today);
            case CYCLE_BIMONTHLY -> resolveByMonthInterval(anchorDt, 2, today);
            case CYCLE_QUARTERLY -> resolveByMonthInterval(anchorDt, 3, today);
            case CYCLE_SEMIANNUAL -> resolveByMonthInterval(anchorDt, 6, today);
            case CYCLE_YEARLY -> resolveByMonthInterval(anchorDt, 12, today);
            default -> null;
        };
    }

    /** 기준일부터 dayInterval일 간격으로 today 이후(포함) 첫 날짜를 찾음 */
    private LocalDate resolveByDayInterval(LocalDate anchorDt, int dayInterval, LocalDate today) {
        LocalDate due = anchorDt;
        while (due.isBefore(today)) {
            due = due.plusDays(dayInterval);
        }
        return due;
    }

    /**
     * 기준일부터 monthInterval개월 간격으로 today 이후(포함) 첫 날짜를 찾음.
     * 카드사 결제일 관행과 동일하게, 기준일의 "일(day)"이 대상 월의 마지막 날보다 크면
     * 다음 달로 넘기지 않고 그 달의 마지막 날로 당겨서 처리한다.
     * 예: 기준일이 31일이고 대상 월이 2월이면 2월 28일(윤년은 29일)로 계산.
     */
    private LocalDate resolveByMonthInterval(LocalDate anchorDt, int monthInterval, LocalDate today) {
        YearMonth targetMonth = YearMonth.from(anchorDt);
        LocalDate due = clampToMonth(targetMonth, anchorDt.getDayOfMonth());
        while (due.isBefore(today)) {
            targetMonth = targetMonth.plusMonths(monthInterval);
            due = clampToMonth(targetMonth, anchorDt.getDayOfMonth());
        }
        return due;
    }

    /** anchorDay가 month의 마지막 날보다 크면 마지막 날로 당겨서 날짜를 만듦 */
    private LocalDate clampToMonth(YearMonth month, int anchorDay) {
        return month.atDay(Math.min(anchorDay, month.lengthOfMonth()));
    }

    /** 고정지출 등록 */
    @Transactional
    public FixedExpenseResponse addFixedExpense(String loginId, String paymentMethodCd, String expenseCycleCd, String categoryCd, LocalDate anchorDt, String fileGroupId, BigDecimal amount, String content, String memo) {
        User user = findActiveUser(loginId);

        FixedExpense fixedExpense = FixedExpense.builder()
                .userId(user.getUserId())
                .paymentMethodCd(paymentMethodCd)
                .expenseCycleCd(expenseCycleCd)
                .categoryCd(categoryCd)
                .anchorDt(anchorDt)
                .fileGroupId(fileGroupId)
                .amount(amount)
                .content(content)
                .memo(memo)
                .useAt("Y")
                .delAt("N")
                .rgsUserId(loginId)
                .build();
        fixedExpenseRepository.save(fixedExpense);

        return toResponses(List.of(fixedExpense)).getFirst();
    }

    /** 고정지출 수정. 본인 소유이면서 삭제되지 않은 건만 수정 가능 */
    @Transactional
    public FixedExpenseResponse updateFixedExpense(String loginId, Long fixedExpenseId, String paymentMethodCd, String expenseCycleCd, String categoryCd, LocalDate anchorDt, String fileGroupId, BigDecimal amount, String content, String memo) {
        User user = findActiveUser(loginId);

        FixedExpense fixedExpense = fixedExpenseRepository.findById(fixedExpenseId)
                .filter(fe -> fe.getUserId().equals(user.getUserId()) && "N".equals(fe.getDelAt()))
                .orElseThrow(() -> new IllegalArgumentException("고정지출을 찾을 수 없습니다."));

        fixedExpense.setPaymentMethodCd(paymentMethodCd);
        fixedExpense.setExpenseCycleCd(expenseCycleCd);
        fixedExpense.setCategoryCd(categoryCd);
        fixedExpense.setAnchorDt(anchorDt);
        fixedExpense.setFileGroupId(fileGroupId);
        fixedExpense.setAmount(amount);
        fixedExpense.setContent(content);
        fixedExpense.setMemo(memo);
        fixedExpense.setUpdUserId(loginId);

        return toResponses(List.of(fixedExpense)).getFirst();
    }
}
