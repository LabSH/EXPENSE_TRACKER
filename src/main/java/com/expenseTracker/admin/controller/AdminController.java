package com.expenseTracker.admin.controller;

import com.expenseTracker.admin.dto.SysLogSearchResponse;
import com.expenseTracker.admin.facade.AdminFacade;
import com.expenseTracker.log.dto.SysLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminFacade adminFacade;

    /** 관리자 대시보드 페이지 뷰 반환 */
    @GetMapping("")
    public String adminIndex() {
        return "admin/index";
    }

    /** 공통코드 관리 페이지 뷰 반환 */
    @GetMapping("/code")
    public String codePage(Model model) {
        model.addAttribute("groups", adminFacade.findCodeGroups());
        return "admin/code/index";
    }

    /** 사용자 관리 페이지 뷰 반환 */
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", adminFacade.findUsers());
        model.addAttribute("stats", adminFacade.getUserStats());
        return "admin/users/index";
    }

    /** 시스템 로그 조회 페이지 뷰 반환 */
    @GetMapping("/logs")
    public String logsPage(@RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           @RequestParam(required = false) String level,
                           Model model) {
        SysLogSearchResponse result = adminFacade.searchLogs(from, to, level);

        model.addAttribute("logs",              result.logs());
        model.addAttribute("from",              result.from());
        model.addAttribute("to",                result.to());
        model.addAttribute("selectedLevel",     result.selectedLevel());
        model.addAttribute("selectedLevelName", result.selectedLevelName());
        return "admin/logs/index";
    }

    /** 기간·레벨 조건으로 시스템 로그 데이터 조회 (Ajax) */
    @GetMapping("/logs/data")
    @ResponseBody
    public List<SysLogResponse> logsData(@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         @RequestParam(required = false) String level) {
        return adminFacade.searchLogs(from, to, level).logs();
    }
}
