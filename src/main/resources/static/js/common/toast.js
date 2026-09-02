/**
 * 공통 토스트 알림
 * - 작업 결과를 화면 우상단에 잠시 띄우고 자동으로 사라진다.
 * - 사용자의 결정이 필요한 안내(삭제 확인 등)는 토스트가 아니라 확인 모달을 쓴다.
 * - 조회처럼 결과가 화면에 바로 드러나는 동작에는 띄우지 않는다.
 *
 * 사용법:
 *   Toast.success('추가되었습니다.');
 *   Toast.delete('삭제되었습니다.');
 *   Toast.error('에러가 발생하였습니다.', '고정지출을 찾을 수 없습니다.');
 *   Toast.info('처리 중입니다.');
 */
(function () {
    'use strict';

    const CONTAINER_ID = 'toast-container';

    // 에러는 내용을 읽어야 하므로 다른 유형보다 오래 남긴다
    const DURATION = { success: 2500, delete: 2500, error: 4000, info: 3000 };

    // input.css의 .anim-toastOut 재생 시간(.24s)과 맞출 것
    const EXIT_MS = 240;

    /**
     * 배경은 중립 surface로 두고 상태색은 테두리와 아이콘 칩에만 쓴다.
     * 여러 개가 쌓여도 색 덩어리가 되지 않고, 사용자 화면(cream)과 관리자 화면(admin) 어디서도
     * 각 테마의 액센트(sage / violet)와 부딪히지 않는다.
     */
    const TYPES = {
        success: {
            border: 'border-green-mid',
            chip: 'bg-green-light text-green',
            path: '<path d="M20 6 9 17l-5-5"/>',
        },
        delete: {
            border: 'border-coral-mid',
            chip: 'bg-coral-light text-coral',
            path: '<path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M6 6v14h12V6"/>',
        },
        error: {
            border: 'border-coral-mid',
            chip: 'bg-coral-light text-coral',
            path: '<circle cx="12" cy="12" r="9"/><path d="M12 8v4"/><path d="M12 16h.01"/>',
        },
        info: {
            border: 'border-blue-mid',
            chip: 'bg-blue-light text-blue',
            path: '<circle cx="12" cy="12" r="9"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
        },
    };

    /**
     * 토스트가 쌓이는 컨테이너를 반환한다. 없으면 만든다.
     * htmx가 통째로 교체하는 #main-content 밖(body 직속)에 두어야 화면 전환에도 살아남는다.
     * z-[60]은 모달(z-50)보다 위에 떠야 하기 때문이다.
     * 폭을 고정해야 문구 길이와 무관하게 쌓인 토스트의 좌우가 맞는다.
     * @returns {HTMLElement}
     */
    function getContainer() {
        let container = document.getElementById(CONTAINER_ID);
        if (!container) {
            container = document.createElement('div');
            container.id = CONTAINER_ID;
            container.className = 'fixed top-6 right-6 z-[60] flex flex-col gap-3 w-85 pointer-events-none';
            document.body.appendChild(container);
        }
        return container;
    }

    /**
     * 토스트 하나를 띄우고 일정 시간 뒤 제거한다. 클릭하면 즉시 닫힌다.
     * @param {'success'|'delete'|'error'|'info'} type
     * @param {string} title 한 줄 요약 (예: '추가되었습니다.')
     * @param {string} [text] 보조 설명. 없으면 제목만 표시한다 (예: 서버 에러 메시지)
     */
    function show(type, title, text) {
        const spec = TYPES[type];
        if (!spec || !title) return;

        const toast = document.createElement('div');
        // 칩(36px)이 한 줄 텍스트(20px)보다 커서 items-center가 아니면 텍스트가 위로 떠 보인다
        toast.className = `anim-toastIn pointer-events-auto flex items-center gap-3.5 p-4 bg-surface border ${spec.border} rounded-xl-1 shadow-soft-md cursor-pointer`;
        toast.innerHTML = `
            <span class="shrink-0 flex items-center justify-center w-9 h-9 rounded-md-1 ${spec.chip}">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">${spec.path}</svg>
            </span>
            <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold tracking-tight text-text leading-5"></p>
                <p class="mt-0.5 text-xs text-text-soft leading-relaxed break-words hidden"></p>
            </div>`;

        // 서버 에러 메시지가 그대로 들어올 수 있으므로 innerHTML이 아닌 textContent로 넣는다
        const [titleEl, textEl] = toast.querySelectorAll('p');
        titleEl.textContent = title;
        if (text) {
            textEl.textContent = text;
            textEl.classList.remove('hidden');
        }

        getContainer().appendChild(toast);

        const remove = () => {
            toast.classList.replace('anim-toastIn', 'anim-toastOut');
            setTimeout(() => toast.remove(), EXIT_MS);
        };

        const timer = setTimeout(remove, DURATION[type]);
        toast.addEventListener('click', () => {
            clearTimeout(timer);
            remove();
        });
    }

    window.Toast = {
        success(title, text) { show('success', title, text); },
        delete(title, text) { show('delete', title, text); },
        error(title, text) { show('error', title, text); },
        info(title, text) { show('info', title, text); },
    };
}());
