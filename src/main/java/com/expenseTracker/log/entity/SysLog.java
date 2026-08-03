package com.expenseTracker.log.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("시스템로그")
@Table(name = "TB_SYS_LOGS", indexes = {
    @Index(name = "idx_sys_logs_01", columnList = "LOG_DT DESC"),
    @Index(name = "idx_sys_logs_02", columnList = "LOG_LEVEL_CD"),
    @Index(name = "idx_sys_logs_03", columnList = "HTTP_STATUS")
})
public class SysLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    @Comment("로그ID")
    private Long logId;

    @CreationTimestamp
    @Column(name = "LOG_DT", nullable = false, updatable = false)
    @Comment("로그일시")
    private LocalDateTime logDt;

    @Column(name = "LOG_LEVEL_CD", length = 30, nullable = false)
    @Comment("로그레벨코드")
    private String logLevelCd;

    @Column(name = "USER_ID", length = 50)
    @Comment("사용자ID")
    private String userId;

    @Column(name = "HTTP_METHOD", length = 10, nullable = false)
    @Comment("HTTP메서드")
    private String httpMethod;

    @Column(name = "REQUEST_URI", length = 500)
    @Comment("요청URI")
    private String requestUri;

    @Column(name = "HTTP_STATUS")
    @Comment("HTTP응답상태코드")
    private Integer httpStatus;

    @Column(name = "RESPONSE_MS")
    @Comment("처리시간(ms)")
    private Integer responseMs;

    @Column(name = "ERROR_MSG", length = 1000)
    @Comment("에러메시지")
    private String errorMsg;

    @Column(name = "CLIENT_IP", length = 45)
    @Comment("클라이언트IP")
    private String clientIp;
}
