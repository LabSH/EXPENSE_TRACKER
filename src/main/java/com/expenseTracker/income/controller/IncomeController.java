package com.expenseTracker.income.controller;

import com.expenseTracker.income.service.IncomeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    /** 수입 페이지 뷰 반환 */
    @GetMapping("/income")
    public String index(HttpServletRequest request, Model model, Authentication auth) {
        if (!isHtmxRequest(request)) return "redirect:/";

        model.addAttribute("incomes", incomeService.findMyIncomes(auth.getName()));
        return "income/index :: content";
    }

    /** HTMX 요청 여부 판별 */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
