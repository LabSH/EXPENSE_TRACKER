/**
 * 공통 토스트 알림
 * - 작업 결과를 화면 우상단에 잠시 띄우고 자동으로 사라진다.
 * - 사용자의 결정이 필요한 안내(삭제 확인 등)는 토스트가 아니라 확인 모달을 쓴다.
 * - 조회처럼 결과가 화면에 바로 드러나는 동작에는 띄우지 않는다.
 *
 * 사용법:
 *   Toast.success('추가되었습니다.');
 *   Toast.error('에러가 발생하였습니다.');
 */
(function () {
    'use strict';

    const CONTAINER_ID = 'toast-container';

    // 에러는 내용을 읽어야 하므로 성공보다 오래 남긴다
    const DURATION = { success: 2500, error: 4000 };

    // input.css의 .anim-toastOut 재생 시간(.24s)과 맞출 것
    const EXIT_MS = 240;

    /**
     * 배경색을 cream/admin surface가 아니라 상태색 계열(sage-light/blush-light)로 쓴다.
     * 두 토큰은 테마 공통이라 사용자 화면과 관리자 화면에서 같은 컴포넌트를 그대로 쓸 수 있다.
     */
    const TYPES = {
        success: {
            box: 'bg-sage-light border-sage/30',
            badge: 'bg-sage text-white',
            path: '<path d="M20 6 9 17l-5-5"/>',
        },
        error: {
            box: 'bg-blush-light border-blush/30',
            badge: 'bg-blush text-white',
            path: '<path d="M18 6 6 18M6 6l12 12"/>',
        },
    };

    /**
     * 토스트가 쌓이는 컨테이너를 반환한다. 없으면 만든다.
     * htmx가 통째로 교체하는 #main-content 밖(body 직속)에 두어야 화면 전환에도 살아남는다.
     * z-[60]은 모달(z-50)보다 위에 떠야 하기 때문이다.
     * @returns {HTMLElement}
     */
    function getContainer() {
        let container = document.getElementById(CONTAINER_ID);
        if (!container) {
            container = document.createElement('div');
            container.id = CONTAINER_ID;
            container.className = 'fixed top-6 right-6 z-[60] flex flex-col items-end gap-2 pointer-events-none';
            document.body.appendChild(container);
        }
        return container;
    }

    /**
     * 토스트 하나를 띄우고 일정 시간 뒤 제거한다. 클릭하면 즉시 닫힌다.
     * 아이콘은 원형 배지로 분리하고, 텍스트는 제목 + 설명 2단으로 구성한다.
     * @param {'success'|'error'} type
     * @param {string} title 한 줄 요약 (예: '추가되었습니다.')
     * @param {string} [text] 보조 설명. 없으면 제목만 표시한다 (예: 서버 에러 메시지)
     */
    function show(type, title, text) {
        const spec = TYPES[type];
        if (!spec || !title) return;

        const toast = document.createElement('div');
        // 배지(28px)와 텍스트 높이가 달라 items-start를 쓰면 한 줄일 때 텍스트가 위로 떠 보인다.
        // 위아래 여백을 맞추기 위해 items-center로 세로 중앙 정렬한다.
        toast.className = `anim-toastIn pointer-events-auto flex items-center gap-3 min-w-60 max-w-sm px-4 py-3 rounded-xl border shadow-lg cursor-pointer ${spec.box}`;
        toast.innerHTML = `
            <span class="shrink-0 flex items-center justify-center w-7 h-7 rounded-full ${spec.badge}">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">${spec.path}</svg>
            </span>
            <div class="min-w-0">
                <p class="text-sm font-semibold text-ink leading-5"></p>
                <p class="mt-0.5 text-xs text-ink-soft leading-4 break-words hidden"></p>
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
        error(title, text) { show('error', title, text); },
    };
}());
