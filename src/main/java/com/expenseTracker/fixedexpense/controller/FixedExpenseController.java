package com.expenseTracker.fixedexpense.controller;

import com.expenseTracker.fixedexpense.dto.AddFixedExpenseRequest;
import com.expenseTracker.fixedexpense.dto.FixedExpenseResponse;
import com.expenseTracker.fixedexpense.facade.FixedExpenseFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FixedExpenseController {

    private final FixedExpenseFacade fixedExpenseFacade;

    /** 고정지출 페이지 뷰 반환 */
    @GetMapping("/fixed-expense")
    public String index(HttpServletRequest request, Model model, Authentication auth) {
        if (!isHtmxRequest(request)) return "redirect:/";

        model.addAttribute("fixedExpenses", fixedExpenseFacade.findMyFixedExpenses(auth.getName()));
        model.addAttribute("paymentMethods", fixedExpenseFacade.findPaymentMethods());
        model.addAttribute("expenseCycles", fixedExpenseFacade.findExpenseCycles());
        model.addAttribute("categories", fixedExpenseFacade.findCategories());
        model.addAttribute("upcoming", fixedExpenseFacade.findUpcoming(auth.getName()));
        return "fixed-expense/index :: content";
    }

    /** 고정지출 등록 */
    @PostMapping("/fixed-expense")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public FixedExpenseResponse add(@Valid @RequestBody AddFixedExpenseRequest req, Authentication auth) {
        return fixedExpenseFacade.addFixedExpense(auth.getName(), req);
    }

    /** 고정지출 수정 */
    @PutMapping("/fixed-expense/{id}")
    @ResponseBody
    public FixedExpenseResponse update(@PathVariable Long id, @Valid @RequestBody AddFixedExpenseRequest req, Authentication auth) {
        return fixedExpenseFacade.updateFixedExpense(auth.getName(), id, req);
    }

    /** 고정지출 삭제 */
    @DeleteMapping("/fixed-expense/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        fixedExpenseFacade.deleteFixedExpense(auth.getName(), id);
    }

    /** 키워드/결제수단/지출주기 조건으로 고정지출 조회 (조건 없으면 전체 목록) */
    @GetMapping("/fixed-expense/data")
    @ResponseBody
    public List<FixedExpenseResponse> search(@RequestParam(defaultValue = "") String keyword,
                                             @RequestParam(defaultValue = "") String paymentMethodCd,
                                             @RequestParam(defaultValue = "") String expenseCycleCd,
                                             Authentication auth) {
        return fixedExpenseFacade.searchFixedExpenses(auth.getName(), keyword, paymentMethodCd, expenseCycleCd);
    }

    /** HTMX 요청 여부 판별 */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
