(function () {
    'use strict';

    const _today = new Date();

    function toStr(d) {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    const todayStr = toStr(_today);

    // 등록된 picker ID 목록 (init 시 채워짐)
    let _ids = [];

    // picker별 상태 (동적으로 키 추가)
    const CAL = { open: null };

    // 페이지별 색상 톤 (기본 admin/violet, 다른 페이지는 init 시 theme 지정)
    const THEMES = {
        admin: {
            surface: 'bg-admin-surface',
            border: 'border-admin-border',
            subtleBg: 'bg-admin-subtle',
            subtleHover: 'hover:bg-admin-subtle',
            borderHoverBg: 'hover:bg-admin-border',
            accentBg: 'bg-violet',
            accentText: 'text-violet',
            accentBorder: 'border-violet',
            accentLightBg: 'bg-violet-light',
            accentLightHover: 'hover:bg-violet-light',
            accentTextHover: 'hover:text-violet',
            accentBgHover: 'hover:bg-violet',
        },
        cream: {
            surface: 'bg-cream-surface',
            border: 'border-cream-border',
            subtleBg: 'bg-cream-subtle',
            subtleHover: 'hover:bg-cream-subtle',
            borderHoverBg: 'hover:bg-cream-border',
            accentBg: 'bg-sage',
            accentText: 'text-sage',
            accentBorder: 'border-sage',
            accentLightBg: 'bg-sage-light',
            accentLightHover: 'hover:bg-sage-light',
            accentTextHover: 'hover:text-sage',
            accentBgHover: 'hover:bg-sage',
        },
    };

    function getTheme(id) {
        return THEMES[CAL[id] && CAL[id].theme] || THEMES.admin;
    }

    // ── 아이콘 ─────────────────────────────────────────────────
    const IC_CHEVL = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>`;
    const IC_CHEVR = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>`;

    const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];
    const MONTHS   = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'];

    // ── 렌더 ───────────────────────────────────────────────────
    function renderCalendar(id) {
        const t    = getTheme(id);
        const el   = document.getElementById(`${id}-cal`);
        const body = CAL[id].mode === 'year'  ? renderYearBody(id)
                   : CAL[id].mode === 'month' ? renderMonthBody(id)
                   :                            renderDayBody(id);
        el.innerHTML = `<div class="${t.surface} border ${t.border} rounded-2xl shadow-xl p-4 w-64">${body}</div>`;
    }

    function renderDayBody(id) {
        const t = getTheme(id);
        const { year, month, date } = CAL[id];
        const minDate = id === 'to'   ? (CAL.from ? CAL.from.date : '') : '';
        const maxDate = id === 'from' ? (CAL.to   ? CAL.to.date   : '') : '';

        const firstDay    = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        const emptyCells = Array.from({ length: firstDay }, () => '<div></div>').join('');
        const dayCells   = Array.from({ length: daysInMonth }, (_, i) => {
            const day = i + 1;
            const str = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isSelected = date === str;
            const isToday    = todayStr === str;
            const isDisabled = (minDate && str < minDate) || (maxDate && str > maxDate);

            let cls = 'h-8 w-full rounded-lg text-xs font-medium transition-all ';
            if      (isSelected) cls += `${t.accentBg} text-white`;
            else if (isToday)    cls += `border ${t.accentBorder} ${t.accentText}`;
            else if (isDisabled) cls += 'text-ink-muted opacity-30 cursor-not-allowed';
            else                 cls += `text-ink ${t.accentLightHover} ${t.accentTextHover}`;

            const click = isDisabled ? '' : `onclick="calSelectDate('${id}','${str}')"`;
            return `<button ${isDisabled ? 'disabled' : ''} ${click} class="${cls}">${day}</button>`;
        }).join('');

        return `
        <div class="flex items-center justify-between mb-3">
            <button onclick="calPrevMonth('${id}')" class="w-7 h-7 rounded-lg ${t.subtleHover} flex items-center justify-center text-ink-soft transition-colors">${IC_CHEVL}</button>
            <button onclick="calSetMode('${id}','year')" class="text-sm font-semibold text-ink ${t.accentTextHover} transition-colors px-2 py-0.5 rounded-lg ${t.subtleHover}">${year}년 ${month + 1}월</button>
            <button onclick="calNextMonth('${id}')" class="w-7 h-7 rounded-lg ${t.subtleHover} flex items-center justify-center text-ink-soft transition-colors">${IC_CHEVR}</button>
        </div>
        <div class="grid grid-cols-7 mb-1">
            ${WEEKDAYS.map(d => `<div class="text-center text-xs text-ink-muted py-1 font-medium">${d}</div>`).join('')}
        </div>
        <div class="grid grid-cols-7 gap-y-0.5">
            ${emptyCells}${dayCells}
        </div>
        <div class="flex gap-2 mt-3 pt-3 border-t ${t.border}">
            <button onclick="calSelectDate('${id}','${todayStr}')" class="flex-1 py-1.5 rounded-lg text-xs font-semibold ${t.accentLightBg} ${t.accentText} ${t.accentBgHover} hover:text-white transition-colors">오늘</button>
            <button onclick="calSelectDate('${id}','')"            class="flex-1 py-1.5 rounded-lg text-xs font-semibold ${t.accentLightBg} ${t.accentText} ${t.accentBgHover} hover:text-white transition-colors">초기화</button>
        </div>`;
    }

    function renderYearBody(id) {
        const t = getTheme(id);
        const { yearRange, year } = CAL[id];
        const yearBtns = Array.from({ length: 12 }, (_, i) => {
            const y = yearRange + i;
            const cls = y === year
                ? `py-2 rounded-xl text-xs font-bold ${t.accentBg} text-white`
                : `py-2 rounded-xl text-xs font-medium text-ink ${t.accentLightHover} ${t.accentTextHover} transition-colors`;
            return `<button onclick="calSelectYear('${id}',${y})" class="${cls}">${y}</button>`;
        }).join('');

        return `
        <div class="flex items-center justify-between mb-4">
            <button onclick="calPrevYearRange('${id}')" class="w-7 h-7 rounded-lg ${t.subtleHover} flex items-center justify-center text-ink-soft transition-colors">${IC_CHEVL}</button>
            <span class="text-sm font-semibold text-ink">${yearRange} – ${yearRange + 11}</span>
            <button onclick="calNextYearRange('${id}')" class="w-7 h-7 rounded-lg ${t.subtleHover} flex items-center justify-center text-ink-soft transition-colors">${IC_CHEVR}</button>
        </div>
        <div class="grid grid-cols-3 gap-1.5">
            ${yearBtns}
        </div>
        <div class="mt-3 pt-3 border-t ${t.border}">
            <button onclick="calSetMode('${id}','day')" class="w-full py-1.5 rounded-lg text-xs font-semibold ${t.subtleBg} text-ink-soft ${t.borderHoverBg} transition-colors">← 돌아가기</button>
        </div>`;
    }

    function renderMonthBody(id) {
        const t = getTheme(id);
        const { year, month } = CAL[id];
        const monthBtns = MONTHS.map((nm, i) => {
            const cls = i === month
                ? `py-2 rounded-xl text-xs font-bold ${t.accentBg} text-white`
                : `py-2 rounded-xl text-xs font-medium text-ink ${t.accentLightHover} ${t.accentTextHover} transition-colors`;
            return `<button onclick="calSelectMonth('${id}',${i})" class="${cls}">${nm}</button>`;
        }).join('');

        return `
        <div class="flex items-center justify-between mb-4">
            <button onclick="calSetMode('${id}','year')" class="w-7 h-7 rounded-lg ${t.subtleHover} flex items-center justify-center text-ink-soft transition-colors">${IC_CHEVL}</button>
            <button onclick="calSetMode('${id}','year')" class="text-sm font-semibold text-ink ${t.accentTextHover} transition-colors px-2 py-0.5 rounded-lg ${t.subtleHover}">${year}년</button>
            <div class="w-7"></div>
        </div>
        <div class="grid grid-cols-3 gap-1.5">
            ${monthBtns}
        </div>
        <div class="mt-3 pt-3 border-t ${t.border}">
            <button onclick="calSetMode('${id}','day')" class="w-full py-1.5 rounded-lg text-xs font-semibold ${t.subtleBg} text-ink-soft ${t.borderHoverBg} transition-colors">← 돌아가기</button>
        </div>`;
    }

    // ── 내부 동작 ──────────────────────────────────────────────
    function setBtnStyle(id, isOpen) {
        const btn = document.getElementById(`${id}-btn`);
        if (!btn) return;
        const t = getTheme(id);
        const activeClasses = [t.accentBorder, t.accentLightBg, t.accentText];
        if (isOpen) {
            btn.classList.add(...activeClasses);
        } else {
            btn.classList.remove(...activeClasses);
        }
    }

    function closePicker(id) {
        document.getElementById(`${id}-cal`).classList.add('hidden');
        setBtnStyle(id, false);
        if (CAL.open === id) CAL.open = null;
    }

    // ── 전역 핸들러 (인라인 onclick에서 호출) ─────────────────
    window.togglePicker = function (id) {
        const other = _ids.find(i => i !== id);
        if (other) closePicker(other);

        if (CAL.open === id) {
            closePicker(id);
        } else {
            CAL.open = id;
            renderCalendar(id);
            const cal = document.getElementById(`${id}-cal`);
            document.body.appendChild(cal); // 조상의 overflow 영향을 받지 않도록 body 직속으로 이동
            const rect = document.getElementById(`${id}-btn`).getBoundingClientRect();
            cal.style.position = 'fixed';
            cal.style.top  = `${rect.bottom + 6}px`;
            cal.style.left = `${rect.left}px`;
            cal.classList.remove('hidden');
            setBtnStyle(id, true);
        }
    };

    window.calPrevMonth = function (id) {
        if (CAL[id].month === 0) { CAL[id].year--; CAL[id].month = 11; }
        else CAL[id].month--;
        renderCalendar(id);
    };

    window.calNextMonth = function (id) {
        if (CAL[id].month === 11) { CAL[id].year++; CAL[id].month = 0; }
        else CAL[id].month++;
        renderCalendar(id);
    };

    window.calSetMode = function (id, mode) {
        CAL[id].mode = mode;
        renderCalendar(id);
    };

    window.calPrevYearRange = function (id) { CAL[id].yearRange -= 12; renderCalendar(id); };
    window.calNextYearRange = function (id) { CAL[id].yearRange += 12; renderCalendar(id); };

    window.calSelectYear = function (id, year) {
        CAL[id].year      = year;
        CAL[id].yearRange = Math.floor(year / 12) * 12;
        CAL[id].mode      = 'month';
        renderCalendar(id);
    };

    window.calSelectMonth = function (id, month) {
        CAL[id].month = month;
        CAL[id].mode  = 'day';
        renderCalendar(id);
    };

    window.calSelectDate = function (id, dateStr) {
        CAL[id].date = dateStr;
        if (dateStr) {
            const d = new Date(dateStr);
            CAL[id].year  = d.getFullYear();
            CAL[id].month = d.getMonth();
        }
        const label = document.getElementById(`${id}-label`);
        if (label) label.textContent = dateStr ? dateStr.replace(/-/g, '.') : '날짜 선택';
        closePicker(id);
    };

    // ── 공개 API ───────────────────────────────────────────────
    window.CalendarPicker = {
        init(ids, theme) {
            _ids = ids;
            const base = Math.floor(_today.getFullYear() / 12) * 12;
            ids.forEach(id => {
                CAL[id] = {
                    date: '', year: _today.getFullYear(), month: _today.getMonth(),
                    mode: 'day', yearRange: base, theme: theme || 'admin',
                };
            });
            CAL.open = null;

            document.addEventListener('mousedown', e => {
                ids.forEach(id => {
                    const wrap  = document.getElementById(`${id}-wrap`);
                    const cal   = document.getElementById(`${id}-cal`);
                    const inside = (wrap && wrap.contains(e.target)) || (cal && cal.contains(e.target));
                    if (CAL.open === id && !inside) {
                        closePicker(id);
                    }
                });
            });
        },

        getDate(id) {
            return CAL[id] ? CAL[id].date : '';
        },

        setDate(id, dateStr) {
            window.calSelectDate(id, dateStr);
        },

        reset(id) {
            window.calSelectDate(id, '');
        },
    };
}());
