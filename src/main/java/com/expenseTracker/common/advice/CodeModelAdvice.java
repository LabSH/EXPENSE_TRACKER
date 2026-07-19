package com.expenseTracker.common.advice;

import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.repository.CodeGroupRepository;
import com.expenseTracker.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class CodeModelAdvice {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeService codeService;

    @ModelAttribute("codes")
    public Map<String, List<CodeResponse>> codes() {
        return codeGroupRepository.findAll().stream()
                .collect(Collectors.toMap(
                        g -> g.getGroupId(),
                        g -> codeService.findCodesByGroup(g.getGroupId()).stream()
                                .filter(c -> "Y".equals(c.useAt()))
                                .toList()
                ));
    }
}
