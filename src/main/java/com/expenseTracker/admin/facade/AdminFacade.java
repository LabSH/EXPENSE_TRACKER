package com.expenseTracker.admin.facade;

import com.expenseTracker.admin.dto.SysLogSearchResponse;
import com.expenseTracker.code.dto.GroupResponse;
import com.expenseTracker.code.service.CodeService;
import com.expenseTracker.log.service.SysLogService;
import com.expenseTracker.user.dto.UserResponse;
import com.expenseTracker.user.dto.UserStats;
import com.expenseTracker.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/** 관리자 화면에서 필요한 여러 도메인 서비스 호출을 조합한다. */
@Service
@RequiredArgsConstructor
public class AdminFacade {

    private static final String LOG_LEVEL_GROUP_ID = "LOGLEVEL";
    private static final String LOG_LEVEL_ALL_LABEL = "로그레벨";

    private final CodeService codeService;
    private final UserAdminService userAdminService;
    private final SysLogService sysLogService;

    /** 공통코드 관리 화면용 그룹 목록 조회 */
    public List<GroupResponse> findCodeGroups() {
        return codeService.findAllGroups();
    }

    /** 사용자 관리 화면용 사용자 목록 조회 */
    public List<UserResponse> findUsers() {
        return userAdminService.findAllUsers();
    }

    /** 사용자 관리 화면용 사용자 통계 조회 */
    public UserStats getUserStats() {
        return userAdminService.getStats();
    }

    /**
     * 시스템 로그 검색. 기간이 비어 있으면 오늘 날짜로, 레벨이 비어 있으면 전체로 간주하며
     * 화면 표시에 필요한 선택된 레벨명을 공통코드에서 함께 조회한다.
     */
    public SysLogSearchResponse searchLogs(String from, String to, String level) {
        String today    = LocalDate.now().toString();
        String fromDate = StringUtils.hasText(from)  ? from  : today;
        String toDate   = StringUtils.hasText(to)    ? to    : today;
        String levelCd  = StringUtils.hasText(level) ? level : "";

        String levelName = levelCd.isEmpty()
                ? LOG_LEVEL_ALL_LABEL
                : codeService.findCodeName(LOG_LEVEL_GROUP_ID, levelCd).orElse(LOG_LEVEL_ALL_LABEL);

        return new SysLogSearchResponse(
                sysLogService.searchLogs(fromDate, toDate, levelCd),
                fromDate,
                toDate,
                levelCd,
                levelName);
    }
}
