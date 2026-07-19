package com.expenseTracker.log.repository;

import com.expenseTracker.log.dto.SysLogResponse;
import com.expenseTracker.log.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysLogRepository extends JpaRepository<SysLog, Long> {

    @Query("""
            SELECT new com.expenseTracker.log.dto.SysLogResponse(
                s.logDt, c.codeNm, s.userId,
                s.httpMethod, s.requestUri,
                s.httpStatus, s.responseMs,
                s.errorMsg, s.clientIp)
            FROM SysLog s, Code c
            WHERE c.id.codeId = s.logLevelCd
              AND c.id.groupId = 'LOGLEVEL'
              AND s.logDt >= :from
              AND s.logDt <= :to
              AND (:levelCd = '' OR s.logLevelCd = :levelCd)
            ORDER BY s.logDt DESC
            """)
    List<SysLogResponse> search(@Param("from")    LocalDateTime from,
                                @Param("to")      LocalDateTime to,
                                @Param("levelCd") String        levelCd);
}
