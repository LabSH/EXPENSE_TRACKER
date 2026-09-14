package com.expenseTracker.common.exception;

/**
 * 비즈니스 규칙 위반(없는 리소스, 중복, 상태 불일치 등)을 나타내는 예외.
 *
 * <p>이 예외의 message 는 개발자가 직접 작성한 사용자 안내 문구이므로 Ajax 응답에 그대로 담는다.
 * 반대로 프레임워크·DB·런타임 예외의 message 는 JPQL/SQL·스키마·클래스명 등 내부 정보를 포함할 수 있어
 * 절대 응답에 담지 않는다. 따라서 "사용자에게 보여줄 메시지"는 반드시 이 타입으로만 던진다.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
