package com.expenseTracker.fixedexpense.service;

import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.fixedexpense.dto.FixedExpenseResponse;
import com.expenseTracker.fixedexpense.dto.FixedExpenseSummaryResponse;
import com.expenseTracker.fixedexpense.dto.FixedExpenseUpcomingResponse;
import com.expenseTracker.fixedexpense.dto.FixedExpenseUpcomingItemResponse;
import com.expenseTracker.fixedexpense.entity.FixedExpense;
import com.expenseTracker.fixedexpense.repository.FixedExpenseRepository;
import com.expenseTracker.user.entity.User;
import com.expenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixedExpenseService {

    public static final String PAYMENT_METHOD_GROUP_ID = "PAYMENTMETHOD";
    public static final String EXPENSE_CYCLE_GROUP_ID = "EXPENSECYCLE";
    public static final String CATEGORY_GROUP_ID = "CATEGORY";

    // EXPENSECYCLE 공통코드 그룹의 코드ID (DATABASE/DML/TB_CO_CODE_EXPENSECYCLE.sql 참조)
    private static final String CYCLE_DAILY = "EXPENSECYCLE_001";
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

    /** 지출주기 코드를 해석한 결과. monthBased면 개월 간격, 아니면 일 간격 */
    private record Cycle(boolean monthBased, int interval) {}

    /** 지출주기 코드를 주기 단위와 간격으로 해석. 알 수 없는 코드면 null */
    private static Cycle cycleOf(String expenseCycleCd) {
        if (expenseCycleCd == null) return null;

        return switch (expenseCycleCd) {
            case CYCLE_DAILY -> new Cycle(false, 1);
            case CYCLE_WEEKLY -> new Cycle(false, 7);
            case CYCLE_BIWEEKLY -> new Cycle(false, 14);
            case CYCLE_MONTHLY -> new Cycle(true, 1);
            case CYCLE_BIMONTHLY -> new Cycle(true, 2);
            case CYCLE_QUARTERLY -> new Cycle(true, 3);
            case CYCLE_SEMIANNUAL -> new Cycle(true, 6);
            case CYCLE_YEARLY -> new Cycle(true, 12);
            default -> null;
        };
    }

    /**
     * 기준일과 지출주기로부터 오늘 이후 첫 지출 예정일을 계산.
     * 기준일이 없거나(anchorDt) 알 수 없는 주기코드면 null 반환.
     */
    private LocalDate calculateNextDueDate(String expenseCycleCd, LocalDate anchorDt, LocalDate today) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || cycle == null) return null;

        return cycle.monthBased()
                ? resolveByMonthInterval(anchorDt, cycle.interval(), today)
                : resolveByDayInterval(anchorDt, cycle.interval(), today);
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

    /**
     * 기준일부터 주기적으로 발생하는 지출일 중 [from, to] 구간(양끝 포함)에 드는 날짜를 오름차순 반환.
     * 기준일이 to보다 뒤인(아직 시작 전) 항목은 빈 목록.
     */
    private List<LocalDate> resolveDueDatesBetween(String expenseCycleCd, LocalDate anchorDt, LocalDate from, LocalDate to) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || cycle == null) return List.of();

        List<LocalDate> dueDates = new ArrayList<>();
        if (cycle.monthBased()) {
            // 말일 보정이 누적으로 어긋나지 않도록 기준월에서 개월수를 더해가며 매번 다시 계산한다
            YearMonth targetMonth = YearMonth.from(anchorDt);
            for (LocalDate due = clampToMonth(targetMonth, anchorDt.getDayOfMonth()); !due.isAfter(to); ) {
                if (!due.isBefore(from)) dueDates.add(due);
                targetMonth = targetMonth.plusMonths(cycle.interval());
                due = clampToMonth(targetMonth, anchorDt.getDayOfMonth());
            }
        } else {
            for (LocalDate due = anchorDt; !due.isAfter(to); due = due.plusDays(cycle.interval())) {
                if (!due.isBefore(from)) dueDates.add(due);
            }
        }
        return dueDates;
    }

    /** 1개월보다 긴 주기의 연간 결제 횟수. 적립 대상이 아닌 주기면 0 */
    private static int reserveYearlyCount(String expenseCycleCd) {
        if (expenseCycleCd == null) return 0;

        return switch (expenseCycleCd) {
            case CYCLE_BIMONTHLY -> 6;
            case CYCLE_QUARTERLY -> 4;
            case CYCLE_SEMIANNUAL -> 2;
            case CYCLE_YEARLY -> 1;
            default -> 0;
        };
    }

    /**
     * 고정지출 논리 삭제. 본인 소유이면서 삭제되지 않은 건만 삭제 가능하다.
     * 딸린 첨부파일을 함께 정리할 수 있도록 파일그룹ID를 반환한다.
     */
    @Transactional
    public String deleteFixedExpense(String loginId, Long fixedExpenseId) {
        User user = findActiveUser(loginId);

        FixedExpense fixedExpense = fixedExpenseRepository.findById(fixedExpenseId)
                .filter(fe -> fe.getUserId().equals(user.getUserId()) && "N".equals(fe.getDelAt()))
                .orElseThrow(() -> new IllegalArgumentException("고정지출을 찾을 수 없습니다."));

        fixedExpense.setDelAt("Y");
        fixedExpense.setUpdUserId(loginId);

        return fixedExpense.getFileGroupId();
    }

    /** 고정지출 화면 우측 패널 데이터(이번 달 요약 + 다가오는 지출 목록) 조회 */
    @Transactional(readOnly = true)
    public FixedExpenseUpcomingResponse findUpcoming(String loginId) {
        User user = findActiveUser(loginId);
        List<FixedExpense> fixedExpenses = fixedExpenseRepository.findByUserIdAndDelAtOrderByRgsDtDesc(user.getUserId(), "N");
        // 요약과 목록이 서로 다른 날짜를 쓰지 않도록 한 번만 캡처한다
        LocalDate today = LocalDate.now();

        return new FixedExpenseUpcomingResponse(
                calculateSummary(fixedExpenses, today),
                toUpcomingResponses(fixedExpenses, today));
    }

    /** 항목별 다음 지출 예정일을 구해 날짜 오름차순으로 변환. 예정일을 계산할 수 없는 건은 제외 */
    private List<FixedExpenseUpcomingItemResponse> toUpcomingResponses(List<FixedExpense> fixedExpenses, LocalDate today) {
        Map<String, String> expenseCycleNames = codeRepository.findByIdGroupId(EXPENSE_CYCLE_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));

        return fixedExpenses.stream()
                .map(fe -> {
                    LocalDate nextDueDt = calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), today);
                    if (nextDueDt == null) return null;
                    return new FixedExpenseUpcomingItemResponse(
                            nextDueDt,
                            fe.getContent(),
                            expenseCycleNames.getOrDefault(fe.getExpenseCycleCd(), fe.getExpenseCycleCd()),
                            fe.getAmount());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(FixedExpenseUpcomingItemResponse::nextDueDt))
                .toList();
    }

    /**
     * 이번 달 결제 예정액·잔여액과 목돈 대비 월 적립 권장액을 계산.
     * 적립 대상은 1개월보다 긴 주기(격월·분기·반기·매년) 중 이번 달에 결제가 없고 이미 시작된 항목이다.
     * 결제가 있는 달에는 그 금액이 monthlyTotal에 잡히므로 적립에서 빼서 이중 계상을 막는다.
     */
    private FixedExpenseSummaryResponse calculateSummary(List<FixedExpense> fixedExpenses, LocalDate today) {
        YearMonth thisMonth = YearMonth.from(today);
        LocalDate monthStart = thisMonth.atDay(1);
        LocalDate monthEnd = thisMonth.atEndOfMonth();

        BigDecimal monthlyTotal = BigDecimal.ZERO;
        BigDecimal remaining = BigDecimal.ZERO;
        BigDecimal yearlyReserve = BigDecimal.ZERO;

        for (FixedExpense fe : fixedExpenses) {
            if (fe.getAmount() == null) continue;

            List<LocalDate> dueDates = resolveDueDatesBetween(fe.getExpenseCycleCd(), fe.getAnchorDt(), monthStart, monthEnd);
            long remainingCount = dueDates.stream().filter(due -> !due.isBefore(today)).count();
            monthlyTotal = monthlyTotal.add(fe.getAmount().multiply(BigDecimal.valueOf(dueDates.size())));
            remaining = remaining.add(fe.getAmount().multiply(BigDecimal.valueOf(remainingCount)));

            int yearlyCount = reserveYearlyCount(fe.getExpenseCycleCd());
            boolean alreadyStarted = fe.getAnchorDt() != null && !fe.getAnchorDt().isAfter(monthEnd);
            if (dueDates.isEmpty() && yearlyCount > 0 && alreadyStarted) {
                yearlyReserve = yearlyReserve.add(fe.getAmount().multiply(BigDecimal.valueOf(yearlyCount)));
            }
        }

        // 항목별로 나누면 반올림 오차가 누적되므로 연 환산액을 모두 더한 뒤 마지막에 한 번만 나눈다
        return new FixedExpenseSummaryResponse(
                monthlyTotal.setScale(0, RoundingMode.HALF_UP),
                yearlyReserve.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP),
                remaining.setScale(0, RoundingMode.HALF_UP));
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
