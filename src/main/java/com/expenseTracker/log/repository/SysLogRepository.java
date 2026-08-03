package com.expenseTracker.log.repository;

import com.expenseTracker.log.dto.SysLogResponse;
import com.expenseTracker.log.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysLogRepository extends JpaRepository<SysLog, Long> {

    /** 기간·레벨 조건으로 시스템 로그 검색 (레벨명은 코드테이블 조인으로 함께 조회) */
    @Query("""
            SELECT new com.expenseTracker.log.dto.SysLogResponse(
                M1.logDt, C1.codeNm, M1.userId,
                M1.httpMethod, M1.requestUri,
                M1.httpStatus, M1.responseMs,
                M1.errorMsg, M1.clientIp)
            FROM SysLog M1, Code C1
            WHERE C1.id.codeId = M1.logLevelCd
              AND C1.id.groupId = 'LOGLEVEL'
              AND M1.logDt >= :from
              AND M1.logDt <= :to
              AND (:levelCd = '' OR M1.logLevelCd = :levelCd)
            ORDER BY M1.logDt DESC
            """)
    List<SysLogResponse> search(@Param("from")    LocalDateTime from,
                                @Param("to")      LocalDateTime to,
                                @Param("levelCd") String        levelCd);
}
