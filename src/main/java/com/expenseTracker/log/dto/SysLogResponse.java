package com.expenseTracker.log.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SysLogResponse(
        String  time,
        String  level,
        String  user,
        String  httpMethod,
        String  requestUri,
        Integer httpStatus,
        Integer responseMs,
        String  errorMsg,
        String  clientIp
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SysLogResponse(LocalDateTime logDt, String level, String user,
                          String httpMethod, String requestUri,
                          Integer httpStatus, Integer responseMs,
                          String errorMsg, String clientIp) {
        this(logDt != null ? logDt.format(FMT) : "",
             level, user, httpMethod, requestUri,
             httpStatus, responseMs, errorMsg, clientIp);
    }
}
