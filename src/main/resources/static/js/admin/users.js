/**
 * 관리자 > 사용자 관리 화면 스크립트.
 * templates/admin/users/index.html 에서 사용한다.
 */

var AVATARS = ['#BB4B1F', '#4579c8', '#d95f3b'];
var ROLE_COLOR = {
    'ROLE_ADMIN':  'bg-admin-accent-light text-admin-accent',
    'ROLE_USER':   'bg-sky-light text-sky',
    'ROLE_VIEWER': 'bg-blush-light text-blush'
};
var ROLE_DISPLAY = { 'ROLE_ADMIN': '관리자', 'ROLE_USER': '사용자', 'ROLE_VIEWER': '뷰어' };
var ROLE_CD_MAP  = { '관리자': 'ROLE_ADMIN', '사용자': 'ROLE_USER', '뷰어': 'ROLE_VIEWER' };

var IC = {
    plus:    'M12 5v14M5 12h14',
    close:   'M18 6 6 18M6 6l12 12',
    user:    'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8',
    edit:    'M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z',
    trash:   'M3 6h18m-2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2',
    refresh: 'M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15',
    eye:     'M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8zM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z',
    eyeOff:  'M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22'
};

function icon(d, size=18, cls='') {
    return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="${cls}"><path d="${d}"/></svg>`;
}

// ── 상태 ─────────────────────────────────────────────────────────
// users, stats 는 템플릿의 th:inline 스크립트에서 서버 데이터로 초기화된다.
var selectedUserId = null;
var newUserRole = '사용자';
var changingRoleCd = null;
var showPwVisible = false;
var userKeyword = '';

// ── 모달 유틸 ─────────────────────────────────────────────────────
function openModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.remove('hidden');
    const box = modal.querySelector('.anim-slideUp');
    if (box) {
        box.classList.remove('anim-slideUp');
        void box.offsetWidth;
        box.classList.add('anim-slideUp');
    }
}
function closeModal(id) { document.getElementById(id)?.classList.add('hidden'); }

// ── 렌더링 ───────────────────────────────────────────────────────
function renderUsersPanel() {
    document.getElementById('users-panel').innerHTML = `
        <div class="grid gap-5 items-start grid-cols-2">
            ${renderUserList()}
            ${renderUserDetail()}
        </div>`;
}

function renderUserList() {
    const filtered = users;

    const header = `
        <div class="px-7 py-5 border-b border-admin-border flex items-center justify-between">
            <h3 class="text-sm font-bold text-ink">사용자 목록</h3>
            <button onclick="openModal('modal-user-add')"
                    class="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-admin-accent text-white text-sm font-semibold hover:opacity-90 transition-opacity border border-transparent">
                ${icon(IC.plus, 13)}<span>사용자 추가</span>
            </button>
        </div>`;

    const searchBar = `
        <div class="px-4 py-3 border-b border-admin-border flex items-center gap-2">
            <div class="relative flex-1">
                <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 text-ink-muted pointer-events-none" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
                <input id="user-keyword" type="text" value="${escapeHtml(userKeyword)}" placeholder="이름 검색..."
                       class="w-full pl-8 pr-3.5 py-1.5 rounded-xl border border-admin-border bg-admin text-sm text-ink outline-none focus:border-admin-accent transition-colors"
                       onkeydown="if(event.key==='Enter') searchUsers()" />
            </div>
            <button onclick="searchUsers()" class="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-admin-accent text-white text-sm font-semibold hover:opacity-90 transition-opacity shrink-0 border border-transparent"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg><span>검색</span></button>
            <button onclick="resetUserSearch()" class="flex items-center gap-2 px-3.5 py-1.5 rounded-xl border border-admin-border text-sm font-medium text-ink-soft hover:bg-admin-subtle transition-colors shrink-0"><span>초기화</span></button>
        </div>`;

    if (filtered.length === 0) {
        return `
            <div class="bg-admin-surface border border-admin-border rounded-2xl overflow-hidden">
                ${header}${searchBar}
                <p class="py-12 text-center text-sm text-ink-muted">${userKeyword ? '검색 결과가 없어요' : '등록된 사용자가 없어요'}</p>
            </div>`;
    }

    const rows = filtered.map((u, i) => {
        const border = i < filtered.length - 1 ? 'border-b border-admin-border' : '';
        const isSelected = u.userId === selectedUserId;
        const bg = isSelected ? 'bg-admin-accent-light' : 'hover:bg-admin-subtle';
        const nameColor = isSelected ? 'text-admin-accent' : 'text-ink';
        const roleClass = ROLE_COLOR[u.roleCd] || 'bg-admin-subtle text-ink-muted';
        const active = u.useAt === 'Y';
        const toggleBg = active ? 'bg-admin-accent' : 'bg-admin-border';
        const knobPos = active ? 'left-6' : 'left-1';
        const statusColor = active ? 'text-sage' : 'text-ink-muted';
        const statusText = active ? '활성' : '비활성';
        return `
            <div class="flex items-center gap-3.5 px-7 py-3.5 transition-colors ${border} ${bg} cursor-pointer" onclick="selectUser('${u.userId}')">
                <div class="w-9 h-9 rounded-full shrink-0 flex items-center justify-center text-sm font-bold text-white"
                     style="background:${AVATARS[i % AVATARS.length]}">${escapeHtml(u.userNm[0])}</div>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium ${nameColor}">${escapeHtml(u.userNm)}</p>
                    <p class="text-xs text-ink-muted mt-0.5 font-mono">${escapeHtml(u.loginId)}</p>
                </div>
                <span class="inline-block px-2.5 py-1 rounded-full text-xs font-semibold ${roleClass}">${ROLE_DISPLAY[u.roleCd] || u.roleCd}</span>
                <div onclick="event.stopPropagation();toggleUser('${u.userId}')" class="flex items-center gap-2 cursor-pointer">
                    <span class="text-xs font-medium ${statusColor}">${statusText}</span>
                    <button class="relative w-11 h-6 rounded-full transition-colors duration-200 ${toggleBg}">
                        <span class="absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all duration-200 ${knobPos}"></span>
                    </button>
                </div>
            </div>`;
    }).join('');

    return `
        <div class="bg-admin-surface border border-admin-border rounded-2xl overflow-hidden">
            ${header}${searchBar}${rows}
        </div>`;
}

function renderUserDetail() {
    const u = users.find(x => x.userId === selectedUserId);
    if (!u) {
        return `
            <div class="bg-admin-surface border border-admin-border rounded-2xl flex flex-col items-center justify-center text-center p-10 text-ink-muted min-h-[200px]">
                ${icon(IC.user, 36, 'mb-3 opacity-30')}
                <p class="text-sm font-medium">사용자를 선택하면</p>
                <p class="text-xs mt-1">상세 정보가 표시돼요</p>
            </div>`;
    }

    const idx = users.findIndex(x => x.userId === selectedUserId);
    const avatarColor = AVATARS[idx % AVATARS.length];
    const roleClass = ROLE_COLOR[u.roleCd] || 'bg-admin-subtle text-ink-muted';
    const active = u.useAt === 'Y';
    const statusColor = active ? 'text-sage' : 'text-ink-muted';
    const statusText = active ? '활성' : '비활성';
    const toggleBg = active ? 'bg-admin-accent' : 'bg-admin-border';
    const knobPos = active ? 'left-6' : 'left-1';

    return `
        <div class="anim-up bg-admin-surface border border-admin-border rounded-2xl overflow-hidden">
            <div class="px-6 py-5 border-b border-admin-border bg-admin-accent-light flex items-start justify-between">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-full flex items-center justify-center text-base font-bold text-white shrink-0"
                         style="background:${avatarColor}">${escapeHtml(u.userNm[0])}</div>
                    <div>
                        <p class="text-sm font-bold text-ink">${escapeHtml(u.userNm)}</p>
                        <p class="text-xs text-ink-muted mt-0.5">${escapeHtml(u.email || '')}</p>
                    </div>
                </div>
                <button onclick="deselectUser()" class="w-7 h-7 rounded-full bg-admin-border flex items-center justify-center text-ink-soft hover:bg-admin-subtle transition-colors shrink-0">
                    ${icon(IC.close, 13)}
                </button>
            </div>
            <div class="px-6 py-4 border-b border-admin-border flex flex-col gap-3.5">
                <div class="flex justify-between items-center">
                    <span class="text-xs text-ink-muted">이름</span>
                    <span class="text-sm font-medium text-ink">${escapeHtml(u.userNm)}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-xs text-ink-muted">ID</span>
                    <span class="text-sm font-medium text-ink font-mono">${escapeHtml(u.loginId)}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-xs text-ink-muted">역할</span>
                    <span class="inline-block px-2.5 py-1 rounded-full text-xs font-semibold ${roleClass}">${ROLE_DISPLAY[u.roleCd] || u.roleCd}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-xs text-ink-muted">계정 상태</span>
                    <div onclick="toggleUser('${u.userId}')" class="flex items-center gap-2 cursor-pointer">
                        <span class="text-xs font-medium ${statusColor}">${statusText}</span>
                        <button class="relative w-11 h-6 rounded-full transition-colors duration-200 ${toggleBg}">
                            <span class="absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all duration-200 ${knobPos}"></span>
                        </button>
                    </div>
                </div>
            </div>
            <div class="px-6 py-4 flex flex-col gap-2.5">
                <button onclick="openRoleChange()" class="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl border border-admin-border text-sm font-medium text-ink hover:bg-admin-subtle transition-colors">
                    ${icon(IC.edit, 16, 'text-sky')}<span>역할 변경</span>
                </button>
                <button onclick="openPwReset()" class="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl border border-admin-border text-sm font-medium text-ink hover:bg-admin-subtle transition-colors">
                    ${icon(IC.refresh, 16, 'text-admin-accent')}<span>비밀번호 초기화</span>
                </button>
                <button onclick="openUserDelete()" class="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl border border-blush/30 text-sm font-medium text-blush hover:bg-blush-light transition-colors">
                    ${icon(IC.trash, 16)}<span>계정 삭제</span>
                </button>
            </div>
        </div>`;
}

function updateUserStats() {
    document.getElementById('stat-total').textContent = stats.total;
    document.getElementById('stat-active').textContent = stats.active;
    document.getElementById('stat-inactive').textContent = stats.inactive;
}

async function refreshStats() {
    stats = await requestJson('/admin/users/stats');
    updateUserStats();
}

function selectUser(userId)  { selectedUserId = userId; renderUsersPanel(); }
function deselectUser()       { selectedUserId = null;   renderUsersPanel(); }

async function searchUsers() {
    userKeyword = document.getElementById('user-keyword')?.value.trim() ?? '';
    try {
        users = await requestJson(`/admin/users/data?keyword=${encodeURIComponent(userKeyword)}`);
        renderUsersPanel();
        Toast.info('조회되었습니다.', `총 ${users.length}건`);
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

async function resetUserSearch() {
    userKeyword = '';
    try {
        users = await requestJson('/admin/users/data');
        renderUsersPanel();
        Toast.info('초기화되었습니다.', `총 ${users.length}건`);
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

async function toggleUser(userId) {
    try {
        const updated = await requestJson(`/admin/users/${userId}/status`, { method: 'PATCH' });
        users = users.map(u => u.userId === userId ? updated : u);
        renderUsersPanel();
        refreshStats();
        Toast.success('상태가 변경되었습니다.');
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

// ── 역할 선택 ─────────────────────────────────────────────────────
function setNewUserRole(role) {
    newUserRole = role;
    document.querySelectorAll('.role-btn').forEach(btn => {
        const isActive = btn.dataset.role === role;
        btn.className = `role-btn flex-1 py-2.5 rounded-xl border-2 text-sm font-semibold transition-all ${isActive ? 'border-admin-accent bg-admin-accent-light text-admin-accent' : 'border-admin-border text-ink-soft hover:bg-admin-subtle'}`;
    });
}

function togglePwVisibility() {
    showPwVisible = !showPwVisible;
    const input = document.getElementById('new-user-pw');
    input.type = showPwVisible ? 'text' : 'password';
    document.getElementById('pw-eye-btn').innerHTML = icon(showPwVisible ? IC.eyeOff : IC.eye, 16);
}

// ── CRUD ──────────────────────────────────────────────────────────
async function confirmAddUser() {
    const userNm  = document.getElementById('new-user-name').value.trim();
    const loginId = document.getElementById('new-user-id').value.trim();
    const passwd  = document.getElementById('new-user-pw').value.trim();
    if (!userNm || !loginId || !passwd) return;
    try {
        const added = await requestJson('/admin/users', {
            method: 'POST',
            body: JSON.stringify({ loginId, passwd, userNm, email: null, roleCd: ROLE_CD_MAP[newUserRole] })
        });
        users.push(added);
        document.getElementById('new-user-name').value = '';
        document.getElementById('new-user-id').value = '';
        document.getElementById('new-user-pw').value = '';
        showPwVisible = false;
        document.getElementById('new-user-pw').type = 'password';
        newUserRole = '사용자';
        closeModal('modal-user-add');
        renderUsersPanel();
        refreshStats();
        Toast.success('추가되었습니다.');
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

function openRoleChange() {
    const u = users.find(x => x.userId === selectedUserId);
    if (!u) return;
    const idx = users.findIndex(x => x.userId === selectedUserId);
    document.getElementById('role-change-user-info').innerHTML = `
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0"
             style="background:${AVATARS[idx % AVATARS.length]}">${escapeHtml(u.userNm[0])}</div>
        <div><p class="text-sm font-semibold text-ink">${escapeHtml(u.userNm)}</p><p class="text-xs text-ink-muted font-mono">${escapeHtml(u.loginId)}</p></div>`;
    setChangeRole(u.roleCd);
    document.getElementById('confirm-role-change-btn').onclick = confirmRoleChange;
    openModal('modal-role-change');
}

function setChangeRole(roleCd) {
    changingRoleCd = roleCd;
    document.querySelectorAll('.role-change-btn').forEach(btn => {
        const isActive = btn.dataset.role === roleCd;
        btn.className = `role-change-btn flex-1 py-2.5 rounded-xl border-2 text-sm font-semibold transition-all ${isActive ? 'border-admin-accent bg-admin-accent-light text-admin-accent' : 'border-admin-border text-ink-soft hover:bg-admin-subtle'}`;
    });
}

async function confirmRoleChange() {
    if (!changingRoleCd) return;
    try {
        const updated = await requestJson(`/admin/users/${selectedUserId}/role`, {
            method: 'PATCH',
            body: JSON.stringify({ roleCd: changingRoleCd })
        });
        users = users.map(u => u.userId === selectedUserId ? updated : u);
        closeModal('modal-role-change');
        renderUsersPanel();
        Toast.success('수정되었습니다.');
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

function openPwReset() {
    const u = users.find(x => x.userId === selectedUserId);
    if (!u) return;
    const idx = users.findIndex(x => x.userId === selectedUserId);
    document.getElementById('reset-user-info').innerHTML = `
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0"
             style="background:${AVATARS[idx % AVATARS.length]}">${escapeHtml(u.userNm[0])}</div>
        <div><p class="text-sm font-semibold text-ink">${escapeHtml(u.userNm)}</p><p class="text-xs text-ink-muted font-mono">${escapeHtml(u.loginId)}</p></div>`;
    document.getElementById('confirm-pw-reset-btn').onclick = confirmPwReset;
    openModal('modal-pw-reset');
}

async function confirmPwReset() {
    try {
        await requestJson(`/admin/users/${selectedUserId}/password-reset`, { method: 'POST' });
        closeModal('modal-pw-reset');
        Toast.success('비밀번호가 초기화되었습니다.');
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

function openUserDelete() {
    const u = users.find(x => x.userId === selectedUserId);
    if (!u) return;
    const idx = users.findIndex(x => x.userId === selectedUserId);
    document.getElementById('delete-user-info').innerHTML = `
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0"
             style="background:${AVATARS[idx % AVATARS.length]}">${escapeHtml(u.userNm[0])}</div>
        <div><p class="text-sm font-semibold text-ink">${escapeHtml(u.userNm)}</p><p class="text-xs text-ink-muted font-mono">${escapeHtml(u.loginId)}</p></div>`;
    document.getElementById('delete-user-confirm-text').innerHTML =
        `<span class="font-semibold text-blush">${escapeHtml(u.userNm)}</span>의 계정을 삭제하시겠어요?`;
    document.getElementById('confirm-user-delete').onclick = confirmDeleteUser;
    openModal('modal-user-delete');
}

async function confirmDeleteUser() {
    try {
        await requestJson(`/admin/users/${selectedUserId}`, { method: 'DELETE' });
        users = users.filter(u => u.userId !== selectedUserId);
        selectedUserId = null;
        closeModal('modal-user-delete');
        renderUsersPanel();
        refreshStats();
        Toast.delete('삭제되었습니다.');
    } catch(e) { Toast.error('에러가 발생하였습니다.', e.message); }
}

// ── 초기화 ───────────────────────────────────────────────────────
renderUsersPanel();
updateUserStats();
