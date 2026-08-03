package com.expenseTracker.common.advice;

import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class CodeModelAdvice {

    private final CodeService codeService;

    /** 모든 뷰에서 사용할 수 있도록 그룹ID별 사용중인 공통코드 목록을 모델에 주입 */
    @ModelAttribute("codes")
    public Map<String, List<CodeResponse>> codes() {
        return codeService.findUsableCodesByGroup();
    }
}
