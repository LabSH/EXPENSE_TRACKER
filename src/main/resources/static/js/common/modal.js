/**
 * 공통 모달 열기/닫기 헬퍼
 * - 열기는 기존 slideUp(안쪽 박스)을 그대로 재생하고, 닫기는 대칭이 되도록 modalOut을 재생한다.
 * - 여러 화면에 흩어져 있던 "hidden 토글 + reflow로 애니메이션 강제" 패턴을 한곳으로 모은다.
 *
 * 마크업 전제(기존 컨벤션):
 *   <div id="xxx-modal" class="hidden fixed inset-0 ...">   ← 바깥 컨테이너(대개 백드롭 겸용)
 *     <div class="... anim-slideUp ...">...</div>            ← 안쪽 박스
 *
 * 사용법:
 *   Modal.open('xxx-modal');   // 또는 Modal.open(elementRef)
 *   Modal.close('xxx-modal');
 */
(function () {
    'use strict';

    // input.css의 .anim-modalOut / .anim-modalFadeOut 재생 시간(.18s)과 맞출 것
    const CLOSE_MS = 180;

    // animationend가 오지 않는 경우(화면 밖, 애니메이션 미적용 등)를 대비한 여유
    const FALLBACK_MS = CLOSE_MS + 50;

    /** id 문자열이면 요소로 바꾸고, 요소면 그대로 돌려준다 */
    function resolve(idOrEl) {
        return typeof idOrEl === 'string' ? document.getElementById(idOrEl) : idOrEl;
    }

    /** 컨테이너 안에서 애니메이션을 걸 안쪽 박스(.anim-slideUp)를 찾는다 */
    function innerBox(container) {
        return container.querySelector('.anim-slideUp');
    }

    /** 진행 중인 닫기 타이머/리스너를 취소하고 흔적을 지운다 (닫히는 중 다시 열 때) */
    function cancelPendingClose(container) {
        const pending = container.__modalClose;
        if (!pending) return;
        clearTimeout(pending.timer);
        pending.box.removeEventListener('animationend', pending.onEnd);
        pending.box.classList.remove('anim-modalOut');
        container.__modalClose = null;
    }

    window.Modal = {
        /** 모달을 열고 안쪽 박스의 slideUp 진입 애니메이션을 처음부터 재생한다 */
        open(idOrEl) {
            const container = resolve(idOrEl);
            if (!container) return;

            cancelPendingClose(container);
            container.classList.remove('anim-modalFadeOut', 'hidden');
            container.style.pointerEvents = '';

            const box = innerBox(container);
            if (!box) return;

            // slideUp은 forwards라 재생을 강제하려면 클래스를 뺐다가 reflow 후 다시 넣어야 한다
            box.classList.remove('anim-modalOut', 'anim-slideUp');
            void box.offsetWidth;
            box.classList.add('anim-slideUp');
        },

        /** 닫기 애니메이션을 재생한 뒤 hidden 처리한다. 이미 닫혔거나 닫히는 중이면 아무것도 하지 않는다 */
        close(idOrEl) {
            const container = resolve(idOrEl);
            if (!container || container.classList.contains('hidden')) return;
            if (container.__modalClose) return;

            const box = innerBox(container);
            if (!box) {
                container.classList.add('hidden');
                return;
            }

            // 닫히는 동안 재클릭(더블 제출 등)을 막는다
            container.style.pointerEvents = 'none';
            container.classList.add('anim-modalFadeOut');

            // slideUp(forwards)이 남아 있으면 modalOut이 안 먹으므로 교체한다
            box.classList.remove('anim-slideUp');
            void box.offsetWidth;
            box.classList.add('anim-modalOut');

            /** 닫기 애니메이션이 끝나면(또는 fallback 시각에) hidden 처리하고 다음 열기를 위해 클래스를 되돌린다 */
            function finish() {
                if (!container.__modalClose) return;
                clearTimeout(container.__modalClose.timer);
                box.removeEventListener('animationend', onEnd);
                container.__modalClose = null;

                container.classList.add('hidden');
                container.classList.remove('anim-modalFadeOut');
                container.style.pointerEvents = '';
                box.classList.remove('anim-modalOut');
                box.classList.add('anim-slideUp');
            }

            /** 안쪽 박스 자신의 애니메이션이 끝났을 때만 마무리한다 (자식 애니메이션은 무시) */
            function onEnd(e) {
                if (e.target === box) finish();
            }

            box.addEventListener('animationend', onEnd);
            const timer = setTimeout(finish, FALLBACK_MS);
            container.__modalClose = { timer, onEnd, box };
        },
    };
}());
