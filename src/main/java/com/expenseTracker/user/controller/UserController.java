package com.expenseTracker.user.controller;

import com.expenseTracker.user.model.UserJoinForm;
import com.expenseTracker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 회원가입 페이지 뷰 반환 */
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("joinForm", UserJoinForm.builder().build());
        return "user/join";
    }

    /** 회원가입 처리 */
    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("joinForm") UserJoinForm form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return "user/join";
        }
        try {
            userService.join(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/join";
        }
        return "redirect:/login?joined";
    }

    /** 아이디 중복 확인 */
    @GetMapping("/check-loginid")
    @ResponseBody
    public boolean checkLoginId(@RequestParam String loginId) {
        return !userService.isLoginIdDuplicate(loginId);
    }
}
