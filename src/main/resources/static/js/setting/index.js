/**
 * 설정 화면 스크립트.
 * templates/setting/index.html 에서 사용한다.
 * NOTE: 사용자 관리 영역은 화면 프로토타입용 임시 데이터이며, 추후 서버 연동으로 대체될 예정이다.
 */

let apiKeyVisible = false;
const API_KEY = 'sk-gaegaebu-2026-xxxx-xxxx-abcdef123456';
let debugOn = false;

const ROLE_COLOR = {
    관리자: 'bg-sage-light text-sage',
    편집자: 'bg-sky-light text-sky',
    뷰어:   'bg-blush-light text-blush'
};

let users = [
    { id: 1, name: '김민준', email: 'minjun@example.com',   role: '관리자', active: true  },
    { id: 2, name: '이서연', email: 'seoyeon@example.com',  role: '편집자', active: true  },
    { id: 3, name: '박지훈', email: 'jihoon@example.com',   role: '뷰어',   active: false },
];

function toggleApiKey() {
    apiKeyVisible = !apiKeyVisible;
    document.getElementById('api-key-display').textContent = apiKeyVisible ? API_KEY : '••••••••••••••••••••••';
    const eyeD = apiKeyVisible
        ? 'M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22'
        : 'M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8zM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z';
    document.getElementById('api-key-icon').querySelector('path')?.setAttribute('d', eyeD);
    document.getElementById('api-key-toggle').lastChild.textContent = apiKeyVisible ? '숨기기' : '보기';
}

function toggleDebug() {
    debugOn = !debugOn;
    document.getElementById('debug-toggle').className = `relative w-11 h-6 rounded-full transition-colors duration-200 ${debugOn ? 'bg-sage' : 'bg-cream-border'}`;
    document.getElementById('debug-knob').className  = `absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all duration-200 ${debugOn ? 'left-6' : 'left-1'}`;
}

function toggleUser(id) {
    users = users.map(u => u.id === id ? { ...u, active: !u.active } : u);
    renderUsers();
}

function renderUsers() {
    document.getElementById('user-list').innerHTML = users.map((u, i) => {
        const border    = i < users.length - 1 ? 'border-b border-cream-border' : '';
        const roleClass = ROLE_COLOR[u.role] || 'bg-cream-subtle text-ink-muted';
        const toggleBg  = u.active ? 'bg-sage' : 'bg-cream-border';
        const knobPos   = u.active ? 'left-6'  : 'left-1';
        const statusCls = u.active ? 'text-sage' : 'text-ink-muted';
        return `
            <div class="flex items-center gap-4 px-7 py-4 ${border}">
                <div class="w-10 h-10 rounded-full shrink-0 bg-sage-light flex items-center justify-center text-sm font-bold text-sage">${escapeHtml(u.name[0])}</div>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-ink">${escapeHtml(u.name)}</p>
                    <p class="text-xs text-ink-muted mt-0.5">${escapeHtml(u.email)}</p>
                </div>
                <span class="inline-block px-2.5 py-1 rounded-full text-xs font-semibold ${roleClass}">${escapeHtml(u.role)}</span>
                <div class="flex items-center gap-2">
                    <span class="text-xs ${statusCls}">${u.active ? '활성' : '비활성'}</span>
                    <button onclick="toggleUser(${u.id})" class="relative w-11 h-6 rounded-full transition-colors duration-200 ${toggleBg}">
                        <span class="absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all duration-200 ${knobPos}"></span>
                    </button>
                </div>
            </div>`;
    }).join('');
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', renderUsers);
} else {
    renderUsers();
}
