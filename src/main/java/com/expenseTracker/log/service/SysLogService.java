package com.expenseTracker.log.service;

import com.expenseTracker.log.dto.SysLogResponse;
import com.expenseTracker.log.entity.SysLog;
import com.expenseTracker.log.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysLogService {

    public static final String LEVEL_INFO    = "LOGLEVEL_001";
    public static final String LEVEL_DEBUG   = "LOGLEVEL_002";
    public static final String LEVEL_ERROR   = "LOGLEVEL_003";
    public static final String LEVEL_WARNING = "LOGLEVEL_004";

    private static final Map<String, Integer> PRIORITY = Map.of(
            LEVEL_DEBUG,   1,
            LEVEL_INFO,    2,
            LEVEL_WARNING, 3,
            LEVEL_ERROR,   4
    );

    private static final Map<String, String> NAME_TO_CODE = Map.of(
            "DEBUG",   LEVEL_DEBUG,
            "INFO",    LEVEL_INFO,
            "WARNING", LEVEL_WARNING,
            "ERROR",   LEVEL_ERROR
    );

    @Value("${app.log.min-level:INFO}")
    private String minLevel;

    private final SysLogRepository sysLogRepository;

    /** 최소 로그 레벨 이상인 경우에만 시스템 로그를 별도 트랜잭션으로 기록 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addLog(String levelCd, String userId,
                       String httpMethod, String requestUri,
                       int httpStatus, int responseMs,
                       String errorMsg, String clientIp) {
        String minLevelCd   = NAME_TO_CODE.getOrDefault(minLevel.toUpperCase(), LEVEL_INFO);
        int    minPriority  = PRIORITY.getOrDefault(minLevelCd, 2);
        int    thisPriority = PRIORITY.getOrDefault(levelCd, 2);

        if (thisPriority < minPriority) {
            return;
        }

        sysLogRepository.save(SysLog.builder()
                .logLevelCd(levelCd)
                .userId(userId)
                .httpMethod(httpMethod)
                .requestUri(requestUri)
                .httpStatus(httpStatus)
                .responseMs(responseMs)
                .errorMsg(errorMsg)
                .clientIp(clientIp)
                .build());
    }

    /** 기간·레벨 조건으로 시스템 로그 검색 (기간 미지정 시 전체기간으로 대체) */
    @Transactional(readOnly = true)
    public List<SysLogResponse> searchLogs(String from, String to, String levelCd) {
        LocalDateTime fromDt = (from != null && !from.isBlank())
                ? LocalDate.parse(from).atStartOfDay()
                : LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime toDt = (to != null && !to.isBlank())
                ? LocalDate.parse(to).atTime(23, 59, 59)
                : LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        String level = (levelCd != null && !levelCd.isBlank()) ? levelCd : "";
        return sysLogRepository.search(fromDt, toDt, level);
    }
}
