package com.expenseTracker.setting.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
public class SettingController {

    @GetMapping("")
    public String index(HttpServletRequest request) {
        if ("true".equals(request.getHeader("HX-Request"))) return "setting/index :: content";
        return "redirect:/";
    }
}
