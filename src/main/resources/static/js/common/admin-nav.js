/**
 * 관리자 사이드바 네비게이션 pill 이동 + 사이드바 토글.
 * fragments/admin-sidebar.html 에서 공통으로 사용된다.
 */
(function () {
    function updateAdminPill(link) {
        var pill = document.getElementById('admin-nav-pill');
        var nav  = document.getElementById('admin-sidebar-nav');
        if (!pill || !nav || !link) return;
        pill.style.top    = link.offsetTop + 'px';
        pill.style.height = link.offsetHeight + 'px';
        pill.style.opacity = '1';
        nav.querySelectorAll('[data-page]').forEach(function (l) {
            var active = l === link;
            l.classList.toggle('text-admin-accent',   active);
            l.classList.toggle('font-semibold', active);
            l.classList.toggle('text-ink-soft', !active);
            l.classList.toggle('font-normal',   !active);
        });
    }

    function initAdminPill() {
        var nav  = document.getElementById('admin-sidebar-nav');
        var pill = document.getElementById('admin-nav-pill');
        if (!nav || !pill) return;
        var active = nav.querySelector('[data-page][href="' + window.location.pathname + '"]');
        if (!active) { pill.style.opacity = '0'; return; }
        pill.style.transition = 'none';
        updateAdminPill(active);
        requestAnimationFrame(function () {
            requestAnimationFrame(function () {
                pill.style.transition = 'top .28s cubic-bezier(.4,0,.2,1), height .28s cubic-bezier(.4,0,.2,1)';
            });
        });
    }

    /* 사이드바 토글 — 콘텐츠 교체 후에도 새 버튼에 재바인딩 */
    function initSidebarToggle() {
        var btn = document.getElementById('sidebar-toggle');
        if (!btn) return;
        btn.addEventListener('click', function () {
            var wrap = document.getElementById('sidebar-wrap');
            var isOpen = wrap.style.width !== '0px';
            wrap.style.width       = isOpen ? '0px'   : '224px';
            wrap.style.marginRight = isOpen ? '-224px' : '0';
        });
    }

    /* HTMX 콘텐츠 교체 후 토글 재바인딩 + pill URL 동기화 (뒤로가기 등) */
    document.addEventListener('htmx:afterSettle', function () {
        initSidebarToggle();
        var nav  = document.getElementById('admin-sidebar-nav');
        var pill = document.getElementById('admin-nav-pill');
        if (!nav || !pill) return;
        var active = nav.querySelector('[data-page][href="' + window.location.pathname + '"]');
        if (active) {
            updateAdminPill(active);
        } else {
            pill.style.opacity = '0';
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        initAdminPill();
        initSidebarToggle();
    });

    /* onclick에서 호출 — 클릭 즉시 pill 이동 */
    window.moveAdminPillTo = function (link) {
        var pill = document.getElementById('admin-nav-pill');
        if (pill) pill.style.transition = 'top .28s cubic-bezier(.4,0,.2,1), height .28s cubic-bezier(.4,0,.2,1)';
        updateAdminPill(link);
    };
}());
