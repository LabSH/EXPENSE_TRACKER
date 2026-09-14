package com.expenseTracker.common.advice;

import com.expenseTracker.common.dto.AjaxResponse;
import com.expenseTracker.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Ajax 요청에서 발생한 예외를 공통 응답 구조({@code {success, data, message}})로 변환한다.
 *
 * <p>두 가지 원칙을 지킨다.
 * <ul>
 *   <li>노출 차단 — 클라이언트로 나가는 message 는 {@link BusinessException} 처럼
 *       개발자가 직접 작성한 문구와 Bean Validation 메시지뿐이다.
 *       그 외 예외의 message(JPQL/SQL, 스택트레이스, 예외 클래스명 등)는 서버 로그에만 남긴다.</li>
 *   <li>범위 한정 — JSON 본문을 반환하는 핸들러(@RestController 또는 @ResponseBody 메서드)의 예외만 처리한다.
 *       페이지(뷰 이름) 반환 핸들러의 예외는 원본 그대로 다시 던져
 *       Spring 기본 오류 페이지(templates/error)가 처리하도록 둔다.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 내부 정보를 감춘 채 클라이언트에 내려보내는 기본 문구 */
    private static final String GENERIC_MESSAGE = "요청을 처리하는 중 오류가 발생했습니다.";

    /** 비즈니스 규칙 위반 (없는 리소스, 중복 등) — 개발자가 작성한 사용자 문구이므로 그대로 전달 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AjaxResponse> handleBusiness(BusinessException e, HttpServletRequest request) throws Exception {
        rethrowIfNotJsonHandler(e, request);
        return ResponseEntity.badRequest().body(AjaxResponse.fail(e.getMessage()));
    }

    /** {@code @Valid @RequestBody} 검증 실패 — 첫 번째 필드 오류 메시지를 반환 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AjaxResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) throws Exception {
        rethrowIfNotJsonHandler(e, request);
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "입력값이 올바르지 않습니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResponse.fail(message));
    }

    /**
     * 인증·인가 예외는 Spring Security(ExceptionTranslationFilter)가 401/403으로 처리해야 하므로
     * 아래 catch-all이 500 JSON으로 바꿔버리지 않도록 원본을 그대로 다시 던진다.
     */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public void handleSecurity(Exception e) throws Exception {
        throw e;
    }

    /**
     * 그 외 모든 예외(프레임워크·DB·런타임) — 상세는 서버 로그에만 남기고 일반 문구만 응답한다.
     * 잘못된 JPQL로 인한 IllegalArgumentException, Hibernate SQLGrammarException,
     * DataIntegrityViolationException 등이 여기로 모인다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AjaxResponse> handleUnexpected(Exception e, HttpServletRequest request) throws Exception {
        rethrowIfNotJsonHandler(e, request);

        log.error("Ajax 요청 처리 실패 [{} {}]", request.getMethod(), request.getRequestURI(), e);

        // Spring이 상태코드를 정해 던지는 예외(잘못된 JSON 본문, 파라미터 타입 불일치 등)는 그 상태코드를 유지한다
        HttpStatusCode status = e instanceof ErrorResponse errorResponse
                ? errorResponse.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(AjaxResponse.fail(GENERIC_MESSAGE));
    }

    /**
     * JSON 본문을 반환하는 핸들러가 아니면 원본 예외를 다시 던진다.
     * 원본과 동일한 예외를 던지면 ExceptionHandlerExceptionResolver가 처리하지 않은 것으로 보고
     * 기본 예외 처리 흐름(templates/error)으로 넘긴다.
     */
    private void rethrowIfNotJsonHandler(Exception e, HttpServletRequest request) throws Exception {
        if (!isJsonHandler(request)) {
            throw e;
        }
    }

    /** 현재 요청을 처리한 핸들러가 @RestController 또는 @ResponseBody 메서드인지 판별 */
    private boolean isJsonHandler(HttpServletRequest request) {
        if (!(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return handlerMethod.hasMethodAnnotation(ResponseBody.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), ResponseBody.class);
    }
}
