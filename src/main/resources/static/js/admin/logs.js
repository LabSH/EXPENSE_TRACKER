/**
 * 관리자 > 시스템 로그 화면 스크립트.
 * templates/admin/logs/index.html 에서 사용한다.
 * LOGS, selectedFrom, selectedTo, selectedLevel 은 템플릿의 th:inline 스크립트에서 초기화된다.
 */

var LEVEL_CFG = {
    INFO:    {
        cls:  'bg-sky-light text-sky',
        icon: `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c5.52 0 10-4.48 10-10S17.52 2 12 2 2 6.48 2 12s4.48 10 10 10M12 16v-4M12 8h.01"/></svg>`,
    },
    DEBUG:   {
        cls:  'bg-admin-subtle text-ink-muted',
        icon: `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c5.52 0 10-4.48 10-10S17.52 2 12 2 2 6.48 2 12s4.48 10 10 10M12 16v-4M12 8h.01"/></svg>`,
    },
    WARNING: {
        cls:  'bg-yellow-50 text-yellow-600',
        icon: `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0M12 9v4M12 17h.01"/></svg>`,
    },
    ERROR:   {
        cls:  'bg-blush-light text-blush',
        icon: `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>`,
    },
};

// ── 로그 검색 / 렌더 ──────────────────────────────────────────
/**
 * 로그를 조회해 목록을 갱신한다. 필터 초기화(resetFilter)도 이 함수를 재사용하므로
 * 두 진입점의 토스트 문구가 겹치지 않도록 isReset 플래그로 구분한다.
 */
async function searchLogs(isReset) {
    const from  = CalendarPicker.getDate('from');
    const to    = CalendarPicker.getDate('to');
    const level = Dropdown.getValue('level');

    const params = new URLSearchParams({ from, to, level });
    history.pushState(null, '', `/admin/logs?${params}`);

    try {
        const data = await requestJson(`/admin/logs/data?${params}`);
        renderLogs(data);
        Toast.info(isReset ? '초기화되었습니다.' : '조회되었습니다.', `총 ${data.length}건`);
    } catch (e) {
        Toast.error('에러가 발생하였습니다.', e.message);
    }
}

/** 날짜·레벨 필터를 오늘/전체로 되돌리고 다시 조회한다 */
function resetFilter() {
    const now   = new Date();
    const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    CalendarPicker.setDate('from', today);
    CalendarPicker.setDate('to',   today);
    Dropdown.reset('level');
    searchLogs(true);
}

var currentLogs = [];

function renderLogs(logs) {
    currentLogs = logs;
    document.getElementById('log-count').textContent = `총 ${logs.length}건`;

    const list = document.getElementById('log-list');
    if (logs.length === 0) {
        list.innerHTML = `
            <div class="px-7 py-16 text-center">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-3 text-ink-muted opacity-30">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8ZM14 2v6h6M16 13H8M16 17H8M10 9H8"/>
                </svg>
                <p class="text-sm font-medium text-ink-soft">조회된 로그가 없어요</p>
                <p class="text-xs text-ink-muted mt-1">날짜 범위를 변경해 다시 검색해 주세요</p>
            </div>`;
        return;
    }

    list.innerHTML = logs.map((log, i) => {
        const lc     = LEVEL_CFG[log.level] || LEVEL_CFG.INFO;
        const border = i < logs.length - 1 ? 'border-b border-admin-border' : '';

        const statusCls = !log.httpStatus          ? 'text-ink-muted'
                        : log.httpStatus >= 500    ? 'text-blush font-bold'
                        : log.httpStatus >= 400    ? 'text-yellow-600 font-bold'
                        :                            'text-sage font-semibold';

        return `
            <div class="grid px-7 py-3 items-center hover:bg-admin-subtle transition-colors cursor-pointer ${border}"
                 style="grid-template-columns:140px 85px 130px 65px 270px 55px 70px 110px 1fr"
                 onclick="openLogDetail(${i})">
                <span class="font-mono text-xs text-ink-muted">${escapeHtml(log.time)}</span>
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-bold w-fit ${lc.cls}">
                    ${lc.icon}${escapeHtml(log.level)}
                </span>
                <span class="text-xs text-ink-soft truncate pr-2">${escapeHtml(log.user ?? '—')}</span>
                <span class="text-xs font-mono font-semibold text-ink">${escapeHtml(log.httpMethod ?? '—')}</span>
                <span class="text-xs text-ink truncate pr-2">${escapeHtml(log.requestUri ?? '—')}</span>
                <span class="text-xs ${statusCls}">${log.httpStatus ?? '—'}</span>
                <span class="text-xs text-ink-muted">${log.responseMs != null ? log.responseMs + 'ms' : '—'}</span>
                <span class="text-xs text-ink-muted truncate pr-2">${escapeHtml(log.clientIp ?? '—')}</span>
                <span class="text-xs text-blush truncate">${escapeHtml(log.errorMsg ?? '—')}</span>
            </div>`;
    }).join('');
}

// 초기 렌더 — 서버에서 받은 날짜/레벨로 초기화 후 바로 렌더
(function () {
    CalendarPicker.init(['from', 'to']);
    CalendarPicker.setDate('from', selectedFrom);
    CalendarPicker.setDate('to',   selectedTo);
    document.getElementById('level-wrap').dataset.value = selectedLevel;
    renderLogs(LOGS);
}());

// ── 로그 상세 모달 ────────────────────────────────────────────
function openLogDetail(index) {
    const log = currentLogs[index];
    const lc  = LEVEL_CFG[log.level] || LEVEL_CFG.INFO;

    const statusCls = !log.httpStatus          ? 'text-ink-muted'
                    : log.httpStatus >= 500    ? 'text-blush font-bold'
                    : log.httpStatus >= 400    ? 'text-yellow-600 font-bold'
                    :                            'text-sage font-semibold';

    document.getElementById('detail-level').className =
        `inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-bold w-fit ${lc.cls}`;
    document.getElementById('detail-level').innerHTML = `${lc.icon}${escapeHtml(log.level)}`;

    document.getElementById('detail-time').textContent      = log.time        || '—';
    document.getElementById('detail-user').textContent      = log.user        || '—';
    document.getElementById('detail-method').textContent    = log.httpMethod  || '—';
    document.getElementById('detail-uri').textContent       = log.requestUri  || '—';
    document.getElementById('detail-status').textContent    = log.httpStatus  ?? '—';
    document.getElementById('detail-status').className      = `text-sm font-mono ${statusCls}`;
    document.getElementById('detail-ms').textContent        = log.responseMs != null ? log.responseMs + ' ms' : '—';
    document.getElementById('detail-ip').textContent        = log.clientIp    || '—';
    document.getElementById('detail-error').textContent     = log.errorMsg    || '—';
    document.getElementById('detail-error-wrap').classList.toggle('hidden', !log.errorMsg);

    Modal.open('log-detail-modal');
}

/** 로그 상세 모달을 닫는다 */
function closeLogDetail() {
    Modal.close('log-detail-modal');
}
