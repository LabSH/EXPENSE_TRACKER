package com.expenseTracker.common.advice;

import com.expenseTracker.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** Ajax 오류 응답에 내부 정보(SQL/JPQL 등)가 섞이지 않는지, 페이지 예외 흐름이 유지되는지 검증 */
class GlobalExceptionHandlerTest {

    @RestController
    static class AjaxTestController {
        @GetMapping("/t/biz")
        String biz() {
            throw new BusinessException("이미 사용 중인 아이디입니다.");
        }

        @GetMapping("/t/framework")
        String framework() {
            throw new IllegalArgumentException("org.hibernate.query.SemanticException: SELECT M1.ID FROM Expense M1 ...");
        }
    }

    @Controller
    static class PageTestController {
        @GetMapping("/t/page")
        String page() {
            throw new IllegalArgumentException("SELECT M1.ID FROM Expense M1 ...");
        }

        @GetMapping("/t/page-biz")
        String pageBiz() {
            throw new BusinessException("비즈니스 오류");
        }

        @GetMapping("/t/page-json")
        @ResponseBody
        String pageJson() {
            throw new BusinessException("페이지 컨트롤러의 Ajax 메서드");
        }
    }

    private MockMvc mockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void ajax_business_message_is_returned() throws Exception {
        String body = mockMvc(new AjaxTestController()).perform(get("/t/biz"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("\"success\":false").contains("이미 사용 중인 아이디입니다.");
    }

    @Test
    void ajax_framework_message_is_masked() throws Exception {
        var res = mockMvc(new AjaxTestController()).perform(get("/t/framework")).andReturn().getResponse();
        String body = res.getContentAsString();
        assertThat(res.getStatus()).isEqualTo(500);
        assertThat(body).doesNotContain("SELECT").doesNotContain("Hibernate").doesNotContain("hibernate");
        assertThat(body).contains("요청을 처리하는 중 오류가 발생했습니다.");
    }

    @Test
    void page_handler_exception_propagates() {
        MockMvc mvc = mockMvc(new PageTestController());
        assertThatThrownBy(() -> mvc.perform(get("/t/page")))
                .rootCause().isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mvc.perform(get("/t/page-biz")))
                .rootCause().isInstanceOf(BusinessException.class);
    }

    @Test
    void responsebody_method_in_page_controller_is_treated_as_ajax() throws Exception {
        String body = mockMvc(new PageTestController()).perform(get("/t/page-json"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("페이지 컨트롤러의 Ajax 메서드");
    }
}
