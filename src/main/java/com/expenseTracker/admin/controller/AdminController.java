package com.expenseTracker.admin.controller;

import com.expenseTracker.code.service.CodeService;
import com.expenseTracker.log.dto.SysLogResponse;
import com.expenseTracker.log.service.SysLogService;
import com.expenseTracker.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final CodeService codeService;
    private final UserAdminService userAdminService;
    private final SysLogService sysLogService;

    @GetMapping("")
    public String adminIndex() {
        return "admin/index";
    }

    @GetMapping("/code")
    public String codePage(Model model) {
        model.addAttribute("groups", codeService.findAllGroups());
        return "admin/code/index";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userAdminService.findAllUsers());
        model.addAttribute("stats", userAdminService.getStats());
        return "admin/users/index";
    }

    @GetMapping("/logs")
    public String logsPage(@RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           @RequestParam(required = false) String level,
                           Model model) {
        String today    = LocalDate.now().toString();
        String fromDate = (from  != null && !from.isBlank())  ? from  : today;
        String toDate   = (to    != null && !to.isBlank())    ? to    : today;
        String levelCd  = (level != null && !level.isBlank()) ? level : "";

        String levelName = levelCd.isEmpty() ? "전체"
                : codeService.findCodesByGroup("LOGLEVEL").stream()
                        .filter(c -> c.codeId().equals(levelCd))
                        .map(c -> c.codeNm())
                        .findFirst().orElse("전체");

        model.addAttribute("logs",               sysLogService.search(fromDate, toDate, levelCd));
        model.addAttribute("from",               fromDate);
        model.addAttribute("to",                 toDate);
        model.addAttribute("selectedLevel",      levelCd);
        model.addAttribute("selectedLevelName",  levelName);
        return "admin/logs/index";
    }

    @GetMapping("/logs/data")
    @ResponseBody
    public List<SysLogResponse> logsData(@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         @RequestParam(required = false) String level) {
        String today   = LocalDate.now().toString();
        String fromDate = (from  != null && !from.isBlank())  ? from  : today;
        String toDate   = (to    != null && !to.isBlank())    ? to    : today;
        String levelCd  = (level != null && !level.isBlank()) ? level : "";
        return sysLogService.search(fromDate, toDate, levelCd);
    }
}
