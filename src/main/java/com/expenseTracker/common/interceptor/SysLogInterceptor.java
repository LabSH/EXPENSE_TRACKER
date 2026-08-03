package com.expenseTracker.common.interceptor;

import com.expenseTracker.log.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class SysLogInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "_logStart";

    private final SysLogService sysLogService;

    /** 요청 처리 시작 시각을 기록 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            request.setAttribute(ATTR_START, System.currentTimeMillis());
        }
        return true;
    }

    /** 요청 처리 완료 후 처리시간·상태코드·예외 정보를 시스템 로그로 기록 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }

        try {
            Long startTime  = (Long) request.getAttribute(ATTR_START);
            int  responseMs = startTime != null ? (int) (System.currentTimeMillis() - startTime) : 0;
            int  httpStatus = response.getStatus();

            // @ControllerAdvice 등이 예외를 처리하면 ex는 null이지만
            // DispatcherServlet이 request attribute에 원본 예외를 보존함
            Throwable resolved = ex != null ? ex
                    : (Throwable) request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

            // 예외가 있으면 status가 200이어도 500으로 보정
            if (resolved != null && httpStatus < 500) {
                httpStatus = 500;
            }

            String levelCd;
            if (resolved != null || httpStatus >= 500) {
                levelCd = SysLogService.LEVEL_ERROR;
            } else if (httpStatus >= 400) {
                levelCd = SysLogService.LEVEL_WARNING;
            } else {
                levelCd = SysLogService.LEVEL_INFO;
            }

            String errorMsg = resolved != null ? resolved.getMessage() : null;

            sysLogService.addLog(
                    levelCd,
                    resolveUserId(),
                    request.getMethod(),
                    request.getRequestURI(),
                    httpStatus,
                    responseMs,
                    errorMsg,
                    resolveClientIp(request)
            );
        } catch (Exception e) {
            log.warn("시스템 로그 기록 실패: {}", e.getMessage());
        }
    }

    /** 인증 정보에서 로그인 사용자ID를 추출 (비인증 시 null) */
    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    /** X-Forwarded-For 헤더를 우선하여 클라이언트 IP를 추출 */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
