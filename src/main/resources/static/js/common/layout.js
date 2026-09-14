/**
 * 사용자 화면 공통 레이아웃(사이드바 토글, 활성 메뉴 pill 이동) 스크립트.
 * dashboard/income/expense/fixed-expense/budget/report/setting 등
 * templates/index.html 레이아웃 안에서 공통으로 사용된다.
 */

let currentActiveNav = 'dashboard';

function toggleSidebar() {
    const wrapper = document.getElementById('sidebar-wrapper');
    if (!wrapper) return;
    const isOpen = wrapper.style.width !== '0px';
    wrapper.style.width = isOpen ? '0px' : '224px';
}

function movePill(btn) {
    const pill = document.getElementById('nav-pill');
    if (!pill || !btn) return;
    pill.style.top = btn.offsetTop + 'px';
    pill.style.height = btn.offsetHeight + 'px';
    pill.style.opacity = '1';
}

function setActiveNav(id) {
    currentActiveNav = id;
    const nav = document.getElementById('sidebar-nav');
    const activeBtn = nav ? nav.querySelector(`[data-nav="${id}"]`) : null;
    movePill(activeBtn);

    document.querySelectorAll('[data-nav]').forEach(el => {
        const isActive = el.dataset.nav === id;
        el.classList.toggle('text-sage', isActive);
        el.classList.toggle('font-semibold', isActive);
        el.classList.toggle('text-ink-soft', !isActive);
        el.classList.toggle('font-normal', !isActive);
    });
}

function initNavFromUrl() {
    const pathMap = {
        '/': 'dashboard',
        '/dashboard': 'dashboard',
        '/income': 'income',
        '/expense': 'expense',
        '/fixed-expense': 'fixed-expense',
        '/budget': 'budget',
        '/report': 'report',
        '/setting': 'settings'
    };
    const id = pathMap[window.location.pathname] || 'dashboard';
    const pill = document.getElementById('nav-pill');
    if (pill) {
        pill.style.transition = 'none';
        setActiveNav(id);
        requestAnimationFrame(() => requestAnimationFrame(() => {
            pill.style.transition = 'top .28s cubic-bezier(.4,0,.2,1), height .28s cubic-bezier(.4,0,.2,1)';
        }));
    } else {
        setActiveNav(id);
    }
}

document.addEventListener('DOMContentLoaded', initNavFromUrl);
document.addEventListener('htmx:historyRestore', initNavFromUrl);
