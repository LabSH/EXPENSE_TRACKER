package com.expenseTracker.admin.dto;

import com.expenseTracker.log.dto.SysLogResponse;

import java.util.List;

/** 시스템 로그 화면에 필요한 검색 결과와 선택된 검색 조건 */
public record SysLogSearchResponse(
        List<SysLogResponse> logs,
        String from,
        String to,
        String selectedLevel,
        String selectedLevelName
) {}
