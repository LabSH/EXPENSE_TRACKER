package com.expenseTracker.home.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "dashboard/index :: content";
        return "redirect:/";
    }

    @GetMapping("/income")
    public String income(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "income/index :: content";
        return "redirect:/";
    }

    @GetMapping("/expense")
    public String expense(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "expense/index :: content";
        return "redirect:/";
    }

    @GetMapping("/fixed-expense")
    public String fixedExpense(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "fixed-expense/index :: content";
        return "redirect:/";
    }

    @GetMapping("/budget")
    public String budget(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "budget/index :: content";
        return "redirect:/";
    }

    @GetMapping("/report")
    public String report(HttpServletRequest request) {
        if (isHtmxRequest(request)) return "report/index :: content";
        return "redirect:/";
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
