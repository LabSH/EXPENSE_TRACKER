package com.expenseTracker.common.advice;

import com.expenseTracker.common.dto.AjaxResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Ajax 요청에서 발생한 예외를 공통 응답 구조로 변환한다.
 * 페이지 렌더링 중 발생하는 예외는 Spring 기본 오류 페이지(templates/error)가 처리하도록
 * 의도적으로 catch-all 핸들러를 두지 않는다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 규칙 위반 (없는 리소스, 중복 등) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AjaxResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(AjaxResponse.fail(e.getMessage()));
    }

    /** @Valid @RequestBody 검증 실패 — 첫 번째 필드 오류 메시지를 반환 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AjaxResponse> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "입력값이 올바르지 않습니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResponse.fail(message));
    }
}
