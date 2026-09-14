package com.expenseTracker.fixedexpense.service;

import com.expenseTracker.account.entity.Account;
import com.expenseTracker.account.repository.AccountRepository;
import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.common.exception.BusinessException;
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
    private final AccountRepository accountRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    /** 로그인한 사용자의 고정지출 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> findMyFixedExpenses(String loginId) {
        User user = findActiveUser(loginId);
        return toResponses(fixedExpenseRepository.findByUserIdAndDelAtOrderByRgsDtDesc(user.getUserId(), "N"));
    }

    /** 키워드/계좌/지출주기 조건으로 검색 (빈 값인 조건은 무시) */
    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> searchFixedExpenses(String loginId, String keyword, Long accountId, String expenseCycleCd) {
        User user = findActiveUser(loginId);
        return toResponses(fixedExpenseRepository.search(user.getUserId(), "N", keyword, accountId, expenseCycleCd));
    }

    /** 삭제되지 않은 사용자를 로그인ID로 조회, 없으면 예외 발생 */
    private User findActiveUser(String loginId) {
        return userRepository.findByLoginIdAndDelAt(loginId, "N")
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));
    }

    /** FixedExpense 엔티티 목록을 계좌·지출주기명을 채운 응답 DTO 목록으로 변환 */
    private List<FixedExpenseResponse> toResponses(List<FixedExpense> fixedExpenses) {
        // 목록 안에서 기준일이 흔들리지 않도록 한 번만 캡처한다
        LocalDate today = LocalDate.now();

        // 참조된 계좌를 한 번에 조회해 항목 수만큼 SELECT가 나가지 않게 한다
        List<Long> accountIds = fixedExpenses.stream()
                .map(FixedExpense::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getAccountId, a -> a));

        Map<String, String> expenseCycleNames = codeRepository.findByIdGroupId(EXPENSE_CYCLE_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));
        Map<String, String> categoryNames = codeRepository.findByIdGroupId(CATEGORY_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));

        return fixedExpenses.stream()
                .map(fe -> {
                    Account account = fe.getAccountId() != null ? accounts.get(fe.getAccountId()) : null;
                    return new FixedExpenseResponse(
                        fe.getFixedExpenseId(),
                        fe.getAccountId(),
                        account != null ? account.getAccountTypeCd() : null,
                        account != null ? account.getAccountNm() : null,
                        fe.getAutoPayAt(),
                        fe.getExpenseCycleCd(),
                        expenseCycleNames.getOrDefault(fe.getExpenseCycleCd(), fe.getExpenseCycleCd()),
                        fe.getCategoryCd(),
                        categoryNames.getOrDefault(fe.getCategoryCd(), fe.getCategoryCd()),
                        fe.getAnchorDt(),
                        fe.getEndDt(),
                        calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), today),
                        isEnded(fe, today),
                        fe.getFileGroupId(),
                        fe.getAmount(),
                        fe.getContent(),
                        fe.getMemo(),
                        fe.getRgsDt());
                })
                .toList();
    }

    /**
     * 종료된 고정지출인지 판정.
     *
     * endDt < today 로는 판정할 수 없다. 종료일이 아직 안 지났어도 다음 발생일이 종료일을 넘으면
     * 이미 끝난 것이기 때문이다. (예: 기준일 1/15 매월, 종료일 9/10, 오늘 9/6 → 다음 발생일 9/15 > 9/10)
     * 또 nextDueDt == null 만으로도 판정할 수 없다. 주기코드가 없는 항목도 null이라 구분되지 않는다.
     */
    private boolean isEnded(FixedExpense fe, LocalDate today) {
        return fe.getEndDt() != null
                && calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), today) == null;
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
     * 기준일 기준 index번째(0부터 시작) 지출일.
     * 주기 규칙과 말일 보정을 이 메서드 한 곳에만 두어, 다음 지출일과 회차가 항상 같은 규칙을 따르게 한다.
     *
     * 카드사 결제일 관행과 동일하게, 기준일의 "일(day)"이 대상 월의 마지막 날보다 크면
     * 다음 달로 넘기지 않고 그 달의 마지막 날로 당겨서 처리한다.
     * 예: 기준일이 31일이고 대상 월이 2월이면 2월 28일(윤년은 29일)로 계산.
     */
    private static LocalDate occurrenceOf(Cycle cycle, LocalDate anchorDt, int index) {
        if (cycle.monthBased()) {
            YearMonth targetMonth = YearMonth.from(anchorDt).plusMonths((long) cycle.interval() * index);
            return clampToMonth(targetMonth, anchorDt.getDayOfMonth());
        }
        return anchorDt.plusDays((long) cycle.interval() * index);
    }

    /**
     * 발생일이 target 이상(포함)이 되는 첫 index를 찾음.
     * occurrenceOf가 단조 증가하므로 앞에서부터 훑으면 된다.
     * 사용자당 고정지출이 수십 건 규모라 산술 역산 대신 단순 루프로 둔다.
     */
    private static int firstIndexOnOrAfter(Cycle cycle, LocalDate anchorDt, LocalDate target) {
        int index = 0;
        while (occurrenceOf(cycle, anchorDt, index).isBefore(target)) {
            index++;
        }
        return index;
    }

    /**
     * 기준일과 지출주기로부터 오늘 이후 첫 지출 예정일을 계산.
     * 기준일이 없거나(anchorDt) 알 수 없는 주기코드면 null 반환.
     * 계산된 날짜가 종료일을 넘으면 이미 끝난 고정지출이므로 null을 반환한다.
     * (호출부가 null을 "예정 없음"으로 이미 처리하고 있어 종료분이 자동으로 걸러진다)
     */
    private LocalDate calculateNextDueDate(String expenseCycleCd, LocalDate anchorDt, LocalDate endDt, LocalDate today) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || cycle == null) return null;

        LocalDate due = occurrenceOf(cycle, anchorDt, firstIndexOnOrAfter(cycle, anchorDt, today));
        return (endDt != null && due.isAfter(endDt)) ? null : due;
    }

    /** anchorDay가 month의 마지막 날보다 크면 마지막 날로 당겨서 날짜를 만듦 */
    private static LocalDate clampToMonth(YearMonth month, int anchorDay) {
        return month.atDay(Math.min(anchorDay, month.lengthOfMonth()));
    }

    /**
     * 해당 지출일이 기준일부터 몇 번째 지출인지(1부터 시작).
     * 회차 표시 대상이 아니면 null.
     *
     * 표시 조건이 두 가지다.
     *  - 종료일이 있어야 한다. 끝을 모르면 "38회차"가 진척을 알려주지 못해 정보가 되지 않는다.
     *  - 매일·매주·격주는 제외한다. "412/730회"는 숫자가 의사결정에 기여하지 않는다.
     *    회차 개념 자체가 할부·약정 같은 월 단위 상품에서 나온다.
     */
    private static Integer installmentNoOf(String expenseCycleCd, LocalDate anchorDt, LocalDate endDt, LocalDate dueDate) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || endDt == null || dueDate == null || cycle == null || !cycle.monthBased()) return null;

        return firstIndexOnOrAfter(cycle, anchorDt, dueDate) + 1;
    }

    /**
     * 종료일까지 발생하는 총 회차. 무기한이거나 회차 표시 대상이 아니면 null.
     *
     * 개월수 나눗셈으로 구하면 말일 보정과 어긋난다.
     * 예: 기준일 1/31, 매월, 종료일 2/28 → 개월수는 0이라 1회로 나오지만
     *     실제로는 2/28이 보정되어 종료일과 같으므로 2회다. occurrenceOf로만 판정한다.
     */
    private static Integer totalInstallmentsOf(String expenseCycleCd, LocalDate anchorDt, LocalDate endDt) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || endDt == null || cycle == null || !cycle.monthBased()) return null;
        if (endDt.isBefore(anchorDt)) return null;

        int index = firstIndexOnOrAfter(cycle, anchorDt, endDt);
        int lastIndex = occurrenceOf(cycle, anchorDt, index).isAfter(endDt) ? index - 1 : index;
        return lastIndex + 1;
    }

    /**
     * 기준일부터 주기적으로 발생하는 지출일 중 [from, to] 구간(양끝 포함)에 드는 날짜를 오름차순 반환.
     * 기준일이 to보다 뒤인(아직 시작 전) 항목은 빈 목록.
     */
    private List<LocalDate> resolveDueDatesBetween(String expenseCycleCd, LocalDate anchorDt, LocalDate endDt, LocalDate from, LocalDate to) {
        Cycle cycle = cycleOf(expenseCycleCd);
        if (anchorDt == null || cycle == null) return List.of();

        // 종료일이 구간 안에 있으면 거기까지만 발생시킨다
        LocalDate effectiveTo = (endDt != null && endDt.isBefore(to)) ? endDt : to;
        if (effectiveTo.isBefore(from)) return List.of();

        List<LocalDate> dueDates = new ArrayList<>();
        if (cycle.monthBased()) {
            // 말일 보정이 누적으로 어긋나지 않도록 기준월에서 개월수를 더해가며 매번 다시 계산한다
            YearMonth targetMonth = YearMonth.from(anchorDt);
            for (LocalDate due = clampToMonth(targetMonth, anchorDt.getDayOfMonth()); !due.isAfter(effectiveTo); ) {
                if (!due.isBefore(from)) dueDates.add(due);
                targetMonth = targetMonth.plusMonths(cycle.interval());
                due = clampToMonth(targetMonth, anchorDt.getDayOfMonth());
            }
        } else {
            for (LocalDate due = anchorDt; !due.isAfter(effectiveTo); due = due.plusDays(cycle.interval())) {
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
                .orElseThrow(() -> new BusinessException("고정지출을 찾을 수 없습니다."));

        fixedExpense.setDelAt("Y");
        fixedExpense.setUpdUserId(loginId);

        return fixedExpense.getFileGroupId();
    }

    /**
     * 고정지출 화면 우측 패널 데이터(이번 달 요약 + 다가오는 지출 목록) 조회.
     * 목록과 동일한 검색 조건을 받아 "현재 조회된 고정지출"만 집계에 반영한다. (빈 값 조건은 무시)
     */
    @Transactional(readOnly = true)
    public FixedExpenseUpcomingResponse findUpcoming(String loginId, String keyword, Long accountId, String expenseCycleCd) {
        User user = findActiveUser(loginId);
        List<FixedExpense> fixedExpenses = fixedExpenseRepository.search(user.getUserId(), "N", keyword, accountId, expenseCycleCd);
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
                    LocalDate nextDueDt = calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), today);
                    if (nextDueDt == null) return null;
                    return new FixedExpenseUpcomingItemResponse(
                            nextDueDt,
                            fe.getContent(),
                            expenseCycleNames.getOrDefault(fe.getExpenseCycleCd(), fe.getExpenseCycleCd()),
                            fe.getAmount(),
                            fe.getAutoPayAt(),
                            installmentNoOf(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), nextDueDt),
                            totalInstallmentsOf(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt()));
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

            List<LocalDate> dueDates = resolveDueDatesBetween(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), monthStart, monthEnd);
            long remainingCount = dueDates.stream().filter(due -> !due.isBefore(today)).count();
            monthlyTotal = monthlyTotal.add(fe.getAmount().multiply(BigDecimal.valueOf(dueDates.size())));
            remaining = remaining.add(fe.getAmount().multiply(BigDecimal.valueOf(remainingCount)));

            int yearlyCount = reserveYearlyCount(fe.getExpenseCycleCd());
            boolean alreadyStarted = fe.getAnchorDt() != null && !fe.getAnchorDt().isAfter(monthEnd);
            // 종료된 항목은 이번 달 결제가 없어 dueDates가 항상 비므로, 이 조건이 없으면 적립액에 영구 계상된다
            boolean hasFutureDue = calculateNextDueDate(fe.getExpenseCycleCd(), fe.getAnchorDt(), fe.getEndDt(), today) != null;
            if (dueDates.isEmpty() && yearlyCount > 0 && alreadyStarted && hasFutureDue) {
                yearlyReserve = yearlyReserve.add(fe.getAmount().multiply(BigDecimal.valueOf(yearlyCount)));
            }
        }

        // 항목별로 나누면 반올림 오차가 누적되므로 연 환산액을 모두 더한 뒤 마지막에 한 번만 나눈다
        return new FixedExpenseSummaryResponse(
                monthlyTotal.setScale(0, RoundingMode.HALF_UP),
                yearlyReserve.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP),
                remaining.setScale(0, RoundingMode.HALF_UP));
    }

    /** 종료일은 기준일(시작일)보다 빠를 수 없다 */
    private void validateDateRange(LocalDate anchorDt, LocalDate endDt) {
        if (endDt != null && anchorDt != null && endDt.isBefore(anchorDt)) {
            throw new BusinessException("종료일은 기준일(시작일)보다 빠를 수 없습니다.");
        }
    }

    /**
     * 지정한 계좌가 이 사용자의 것인지 검증한다.
     * ACCOUNT_ID 에는 FK가 없어 타 사용자 계좌ID를 실어 보낼 수 있으므로, 여기가 유일한 방어선이다.
     */
    private void validateAccount(String userId, Long accountId) {
        if (accountId != null && !accountRepository.existsByAccountIdAndUserIdAndDelAt(accountId, userId, "N")) {
            throw new BusinessException("계좌를 찾을 수 없습니다.");
        }
    }

    /** 자동이체여부를 정규화한다. 빈 값이면 수동이체('N') */
    private String normalizeAutoPayAt(String autoPayAt) {
        return "Y".equals(autoPayAt) ? "Y" : "N";
    }

    /** 해당 계좌를 사용 중인(삭제되지 않은) 고정지출이 있는지 확인 */
    @Transactional(readOnly = true)
    public boolean isAccountInUse(Long accountId) {
        return fixedExpenseRepository.countByAccountIdAndDelAt(accountId, "N") > 0;
    }

    /** 고정지출 등록 */
    @Transactional
    public FixedExpenseResponse addFixedExpense(String loginId, Long accountId, String autoPayAt, String expenseCycleCd, String categoryCd, LocalDate anchorDt, LocalDate endDt, String fileGroupId, BigDecimal amount, String content, String memo) {
        validateDateRange(anchorDt, endDt);
        User user = findActiveUser(loginId);
        validateAccount(user.getUserId(), accountId);

        FixedExpense fixedExpense = FixedExpense.builder()
                .userId(user.getUserId())
                .accountId(accountId)
                .autoPayAt(normalizeAutoPayAt(autoPayAt))
                .expenseCycleCd(expenseCycleCd)
                .categoryCd(categoryCd)
                .anchorDt(anchorDt)
                .endDt(endDt)
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
    public FixedExpenseResponse updateFixedExpense(String loginId, Long fixedExpenseId, Long accountId, String autoPayAt, String expenseCycleCd, String categoryCd, LocalDate anchorDt, LocalDate endDt, String fileGroupId, BigDecimal amount, String content, String memo) {
        validateDateRange(anchorDt, endDt);
        User user = findActiveUser(loginId);
        validateAccount(user.getUserId(), accountId);

        FixedExpense fixedExpense = fixedExpenseRepository.findById(fixedExpenseId)
                .filter(fe -> fe.getUserId().equals(user.getUserId()) && "N".equals(fe.getDelAt()))
                .orElseThrow(() -> new BusinessException("고정지출을 찾을 수 없습니다."));

        fixedExpense.setAccountId(accountId);
        fixedExpense.setAutoPayAt(normalizeAutoPayAt(autoPayAt));
        fixedExpense.setExpenseCycleCd(expenseCycleCd);
        fixedExpense.setCategoryCd(categoryCd);
        fixedExpense.setAnchorDt(anchorDt);
        fixedExpense.setEndDt(endDt);
        fixedExpense.setFileGroupId(fileGroupId);
        fixedExpense.setAmount(amount);
        fixedExpense.setContent(content);
        fixedExpense.setMemo(memo);
        fixedExpense.setUpdUserId(loginId);

        return toResponses(List.of(fixedExpense)).getFirst();
    }
}
