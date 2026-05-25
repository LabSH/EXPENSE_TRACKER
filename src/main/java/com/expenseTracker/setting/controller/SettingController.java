package com.expenseTracker.setting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
public class SettingController {

    /** 설정 메인 페이지 뷰 반환 */
    @GetMapping("")
    public String index() {
        return "setting/index";
    }

}
