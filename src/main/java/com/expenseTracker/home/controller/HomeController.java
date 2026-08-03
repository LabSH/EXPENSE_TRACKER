package com.expenseTracker.home.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** 메인 페이지 뷰 반환 */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** 대시보드 페이지(또는 HTMX 조각) 뷰 반환 */
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "dashboard/index :: content";
        return "redirect:/";
    }

    /** 지출 페이지(또는 HTMX 조각) 뷰 반환 */
    @GetMapping("/expense")
    public String expense(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "expense/index :: content";
        return "redirect:/";
    }

    /** 예산 페이지(또는 HTMX 조각) 뷰 반환 */
    @GetMapping("/budget")
    public String budget(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "budget/index :: content";
        return "redirect:/";
    }

    /** 리포트 페이지(또는 HTMX 조각) 뷰 반환 */
    @GetMapping("/report")
    public String report(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "report/index :: content";
        return "redirect:/";
    }

    /** HTMX 요청 여부 판별 */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
