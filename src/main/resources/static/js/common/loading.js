/**
 * 공통 로딩 표시
 * - 요청이 시작되자마자 띄우지 않고, DELAY_MS 이상 걸릴 때만 보여준다.
 *   대부분의 요청은 그보다 훨씬 빨리 끝나므로 화면이 깜빡이지 않는다.
 * - 동시에 여러 요청이 진행될 수 있어 boolean이 아니라 카운터로 관리한다.
 *   boolean이면 먼저 끝난 요청이 아직 진행 중인 요청의 로딩까지 꺼버린다.
 * - 오버레이가 화면을 덮어 같은 버튼을 다시 누르는 중복 요청을 막는다.
 *
 * 사용법 (공통 requestJson이 이미 호출하므로 직접 쓸 일은 드물다):
 *   Loading.begin();
 *   try { ... } finally { Loading.end(); }
 */
(function () {
    'use strict';

    const OVERLAY_ID = 'loading-overlay';

    // 이 시간 이상 걸리는 요청에만 로딩을 보여준다
    const DELAY_MS = 2000;

    let pending = 0;
    let timer = null;

    /**
     * 오버레이를 반환한다. 없으면 만든다.
     * htmx가 교체하는 #main-content 밖(body 직속)에 두고,
     * z-[55]로 모달(z-50) 위·토스트(z-60) 아래에 둔다.
     * @returns {HTMLElement}
     */
    function getOverlay() {
        let overlay = document.getElementById(OVERLAY_ID);
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = OVERLAY_ID;
            overlay.className = 'hidden fixed inset-0 z-[55] flex items-center justify-center bg-ink/20 backdrop-blur-sm';
            overlay.innerHTML = `
                <div class="flex items-center gap-3 px-5 py-4 rounded-xl-1 bg-surface border border-border shadow-soft-md">
                    <span class="w-5 h-5 rounded-full border-2 border-border border-t-green animate-spin"></span>
                    <span class="text-sm font-medium text-text">처리 중입니다...</span>
                </div>`;
            document.body.appendChild(overlay);
        }
        return overlay;
    }

    window.Loading = {
        /** 요청 시작을 알린다. 첫 요청 기준으로 DELAY_MS가 지나면 오버레이가 뜬다 */
        begin() {
            pending += 1;
            if (pending === 1) {
                timer = setTimeout(() => getOverlay().classList.remove('hidden'), DELAY_MS);
            }
        },

        /** 요청 종료를 알린다. 진행 중인 요청이 모두 끝났을 때만 오버레이를 감춘다 */
        end() {
            pending = Math.max(0, pending - 1);
            if (pending === 0) {
                clearTimeout(timer);
                timer = null;
                getOverlay().classList.add('hidden');
            }
        },
    };
}());
