package com.expenseTracker.income.service;

import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.income.dto.IncomeResponse;
import com.expenseTracker.income.repository.IncomeRepository;
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
public class IncomeService {

    private static final String INCOME_TYPE_GROUP_ID = "INCOMETYPE";

    private final IncomeRepository incomeRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    /** 로그인한 사용자의 수입 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<IncomeResponse> findMyIncomes(String loginId) {
        User user = userRepository.findByLoginIdAndDelAt(loginId, "N")
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Map<String, String> incomeTypeNames = codeRepository.findByIdGroupId(INCOME_TYPE_GROUP_ID).stream()
                .collect(Collectors.toMap(c -> c.getId().getCodeId(), Code::getCodeNm));

        return incomeRepository.findByUserIdAndDelAtOrderByIncomeDtDescIncomeIdDesc(user.getUserId(), "N").stream()
                .map(income -> new IncomeResponse(
                        income.getIncomeId(),
                        income.getIncomeTypeCd(),
                        incomeTypeNames.getOrDefault(income.getIncomeTypeCd(), income.getIncomeTypeCd()),
                        income.getIncomeDt(),
                        income.getAmount(),
                        income.getContent(),
                        income.getMemo()))
                .toList();
    }
}
