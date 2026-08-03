package com.expenseTracker.common.dto;

/** Ajax 응답 공통 구조 { success, data, message } */
public record AjaxResponse(boolean success, Object data, String message) {

    /** 성공 응답 (데이터 포함) */
    public static AjaxResponse success(Object data, String message) {
        return new AjaxResponse(true, data, message);
    }

    /** 실패 응답 */
    public static AjaxResponse fail(String message) {
        return new AjaxResponse(false, null, message);
    }
}
