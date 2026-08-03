/**
 * 관리자 > 코드 데이터 관리 화면 스크립트.
 * templates/admin/code/index.html 에서 사용한다.
 */

var codeSelectedGroup = null;
var codeEditingCodeId = null;
var codeItemUseAt = 'Y';
var codeCurrentItems = [];

// ── modal util ───────────────────────────────────────────────────
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

// ── group list (re-render after CRUD) ────────────────────────────
async function reloadGroups() {
    const keyword = document.getElementById('group-keyword')?.value.trim() ?? '';
    const url = keyword ? `/admin/code/groups?keyword=${encodeURIComponent(keyword)}` : '/admin/code/groups';
    const groups = await requestJson(url);
    const list = document.getElementById('group-list');
    if (groups.length === 0) {
        list.innerHTML = '<p class="py-8 text-center text-sm text-ink-muted">그룹이 없어요</p>';
        return;
    }
    list.innerHTML = groups.map((g, i) => {
        const border = i < groups.length - 1 ? 'border-b border-admin-border' : '';
        const active = codeSelectedGroup === g.groupId;
        return `
            <div class="group-item relative flex items-center cursor-pointer transition-colors ${border} ${active ? 'bg-violet-light' : 'hover:bg-admin-subtle'}"
                 data-group-id="${g.groupId}" onclick="selectGroup('${g.groupId}')">
                <div class="flex-1 px-5 py-4 min-w-0">
                    <p class="text-sm font-medium ${active ? 'text-violet' : 'text-ink'}">${escapeHtml(g.groupNm)}</p>
                    <p class="text-xs text-ink-muted mt-0.5">${g.itemCount}개 항목</p>
                </div>
                <div class="group-actions shrink-0 hidden gap-1 pr-3">
                    <button onclick="event.stopPropagation();openEditGroup('${g.groupId}','${escQ(g.groupNm)}')"
                            class="w-7 h-7 rounded-lg bg-admin-surface border border-admin-border flex items-center justify-center text-ink-soft hover:border-violet hover:text-violet transition-colors">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z"/></svg>
                    </button>
                    <button onclick="event.stopPropagation();openDeleteGroup('${g.groupId}','${escQ(g.groupNm)}')"
                            class="w-7 h-7 rounded-lg bg-admin-surface border border-admin-border flex items-center justify-center text-ink-soft hover:border-blush hover:text-blush transition-colors">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18m-2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </div>
            </div>`;
    }).join('');
    bindGroupHover();
}

function bindGroupHover() {
    document.querySelectorAll('.group-item').forEach(row => {
        row.addEventListener('mouseenter', () => row.querySelector('.group-actions')?.classList.replace('hidden','flex'));
        row.addEventListener('mouseleave', () => row.querySelector('.group-actions')?.classList.replace('flex','hidden'));
    });
}

function resetGroupSearch() {
    document.getElementById('group-keyword').value = '';
    reloadGroups();
}

function escQ(s) { return String(s).replace(/'/g, "\\'"); }

// ── group select ─────────────────────────────────────────────────
async function selectGroup(groupId) {
    codeSelectedGroup = groupId;

    document.querySelectorAll('.group-item').forEach(el => {
        const active = el.dataset.groupId === groupId;
        el.classList.toggle('bg-violet-light', active);
        el.classList.toggle('hover:bg-admin-subtle', !active);
        const nm = el.querySelector('p.text-sm.font-medium');
        if (nm) { nm.classList.toggle('text-violet', active); nm.classList.toggle('text-ink', !active); }
    });

    const nm = document.querySelector(`.group-item[data-group-id="${groupId}"] p.text-sm.font-medium`)?.textContent ?? groupId;
    document.getElementById('item-panel-title').textContent = nm;
    const addBtn = document.getElementById('add-item-btn');
    addBtn.classList.remove('hidden');
    addBtn.classList.add('flex');

    const panel = document.getElementById('item-panel');
    panel.classList.remove('anim-up');
    void panel.offsetWidth;
    panel.classList.add('anim-up');

    await reloadItems();
}

// ── item list ────────────────────────────────────────────────────
async function reloadItems() {
    if (!codeSelectedGroup) return;
    const items = await requestJson(`/admin/code/groups/${codeSelectedGroup}/items`);
    codeCurrentItems = items;
    const list = document.getElementById('item-list');
    if (items.length === 0) {
        list.innerHTML = '<p class="py-8 text-center text-sm text-ink-muted">항목이 없어요. 항목을 추가해보세요.</p>';
        return;
    }
    list.innerHTML =
        `<div class="grid px-7 py-2.5 bg-admin-subtle border-b border-admin-border text-xs font-semibold text-ink-muted uppercase tracking-wide" style="grid-template-columns:56px 1fr 80px 136px">
            <span>순번</span><span>항목명</span><span>사용여부</span><span></span>
        </div>` +
        items.map(item => `
            <div class="grid items-center px-7 py-3 border-b border-admin-border last:border-0 hover:bg-admin-subtle transition-colors" style="grid-template-columns:56px 1fr 80px 136px">
                <span class="w-7 h-7 rounded-lg text-xs font-bold flex items-center justify-center bg-violet-light text-violet">${item.sortSn}</span>
                <span class="text-sm text-ink">${escapeHtml(item.codeNm)}</span>
                <span class="inline-block px-2 py-0.5 rounded-full text-xs font-medium w-fit ${item.useAt === 'Y' ? 'bg-sage-light text-sage' : 'bg-admin-subtle text-ink-muted'}">${item.useAt === 'Y' ? '사용' : '미사용'}</span>
                <div class="flex gap-1.5 justify-end">
                    <button onclick="openEditItem('${item.codeId}','${escQ(item.codeNm)}',${item.sortSn},'${item.useAt}')"
                            class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-admin-subtle text-ink-soft border border-admin-border hover:bg-admin-border transition-colors">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z"/></svg><span>수정</span>
                    </button>
                    <button onclick="openDeleteItem('${item.codeId}','${escQ(item.codeNm)}')"
                            class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-blush-light text-blush border border-blush/30 hover:opacity-90 transition-opacity">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18m-2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg><span>삭제</span>
                    </button>
                </div>
            </div>`).join('');
}

// ── group CRUD ───────────────────────────────────────────────────
function openAddGroup() {
    document.getElementById('add-group-id').value = '';
    document.getElementById('add-group-nm').value = '';
    openModal('modal-add-group');
    setTimeout(() => document.getElementById('add-group-id').focus(), 50);
}

async function confirmAddGroup() {
    const groupId = document.getElementById('add-group-id').value.trim();
    const groupNm = document.getElementById('add-group-nm').value.trim();
    if (!groupId || !groupNm) return;
    try {
        await requestJson('/admin/code/groups', { method: 'POST', body: JSON.stringify({ groupId, groupNm }) });
        closeModal('modal-add-group');
        await reloadGroups();
    } catch (e) { alert(e.message); }
}

function openEditGroup(groupId, groupNm) {
    document.getElementById('edit-group-id').value = groupId;
    document.getElementById('edit-group-nm').value = groupNm;
    openModal('modal-edit-group');
    setTimeout(() => document.getElementById('edit-group-nm').focus(), 50);
}

async function confirmEditGroup() {
    const groupId = document.getElementById('edit-group-id').value;
    const groupNm = document.getElementById('edit-group-nm').value.trim();
    if (!groupNm) return;
    try {
        await requestJson(`/admin/code/groups/${groupId}`, { method: 'PATCH', body: JSON.stringify({ groupNm }) });
        closeModal('modal-edit-group');
        if (codeSelectedGroup === groupId) document.getElementById('item-panel-title').textContent = groupNm;
        await reloadGroups();
    } catch (e) { alert(e.message); }
}

function openDeleteGroup(groupId, groupNm) {
    document.getElementById('delete-group-name').textContent = `"${groupNm}"`;
    document.getElementById('confirm-delete-group-btn').onclick = () => confirmDeleteGroup(groupId);
    openModal('modal-delete-group');
}

async function confirmDeleteGroup(groupId) {
    try {
        await requestJson(`/admin/code/groups/${groupId}`, { method: 'DELETE' });
        closeModal('modal-delete-group');
        if (codeSelectedGroup === groupId) {
            codeSelectedGroup = null;
            document.getElementById('item-panel-title').textContent = '그룹을 선택하세요';
            const addBtn = document.getElementById('add-item-btn');
            addBtn.classList.add('hidden');
            addBtn.classList.remove('flex');
            document.getElementById('item-list').innerHTML =
                '<p class="py-8 text-center text-sm text-ink-muted">그룹이 삭제됐어요</p>';
        }
        await reloadGroups();
    } catch (e) { alert(e.message); }
}

// ── item CRUD ────────────────────────────────────────────────────
function adjustSeq(delta) {
    const input = document.getElementById('item-seq');
    input.value = Math.max(1, (parseInt(input.value) || 1) + delta);
}

function setItemUse(val) {
    codeItemUseAt = val;
    document.getElementById('item-use-y').className = `flex-1 py-2.5 rounded-xl border-2 text-sm font-semibold transition-all ${val === 'Y' ? 'border-violet bg-violet-light text-violet' : 'border-admin-border text-ink-soft hover:bg-admin-subtle'}`;
    document.getElementById('item-use-n').className = `flex-1 py-2.5 rounded-xl border-2 text-sm font-semibold transition-all ${val === 'N' ? 'border-violet bg-violet-light text-violet' : 'border-admin-border text-ink-soft hover:bg-admin-subtle'}`;
}

function openAddItem() {
    codeEditingCodeId = null;
    codeItemUseAt = 'Y';
    document.getElementById('modal-item-title').textContent = '항목 추가';
    const nextSeq = codeCurrentItems.length > 0
        ? Math.max(...codeCurrentItems.map(i => i.sortSn)) + 1
        : 1;
    document.getElementById('item-nm').value = '';
    document.getElementById('item-seq').value = nextSeq;
    document.getElementById('item-code-id-wrap').classList.add('hidden');
    setItemUse('Y');
    document.getElementById('modal-item-confirm').textContent = '추가하기';
    document.getElementById('modal-item-confirm').onclick = confirmAddItem;
    openModal('modal-item');
    setTimeout(() => document.getElementById('item-nm').focus(), 50);
}

function openEditItem(codeId, codeNm, sortSn, useAt) {
    codeEditingCodeId = codeId;
    codeItemUseAt = useAt;
    document.getElementById('modal-item-title').textContent = '항목 수정';
    document.getElementById('item-nm').value = codeNm;
    document.getElementById('item-seq').value = sortSn;
    document.getElementById('item-code-id').value = codeId;
    document.getElementById('item-code-id-wrap').classList.remove('hidden');
    setItemUse(useAt);
    document.getElementById('modal-item-confirm').textContent = '저장하기';
    document.getElementById('modal-item-confirm').onclick = confirmEditItem;
    openModal('modal-item');
    setTimeout(() => document.getElementById('item-nm').focus(), 50);
}

async function confirmAddItem() {
    const codeNm = document.getElementById('item-nm').value.trim();
    const sortSn = parseInt(document.getElementById('item-seq').value) || 1;
    if (!codeNm) return;
    try {
        await requestJson(`/admin/code/groups/${codeSelectedGroup}/items`, {
            method: 'POST',
            body: JSON.stringify({ codeNm, sortSn, useAt: codeItemUseAt })
        });
        closeModal('modal-item');
        await reloadItems();
        await reloadGroups();
    } catch (e) { alert(e.message); }
}

async function confirmEditItem() {
    const codeNm = document.getElementById('item-nm').value.trim();
    const sortSn = parseInt(document.getElementById('item-seq').value) || 1;
    if (!codeNm) return;
    try {
        await requestJson(`/admin/code/groups/${codeSelectedGroup}/items/${codeEditingCodeId}`, {
            method: 'PUT',
            body: JSON.stringify({ codeNm, sortSn, useAt: codeItemUseAt })
        });
        closeModal('modal-item');
        await reloadItems();
    } catch (e) { alert(e.message); }
}

function openDeleteItem(codeId, codeNm) {
    document.getElementById('delete-item-name').textContent = `"${codeNm}"`;
    document.getElementById('confirm-delete-item-btn').onclick = () => confirmDeleteItem(codeId);
    openModal('modal-delete-item');
}

async function confirmDeleteItem(codeId) {
    try {
        await requestJson(`/admin/code/groups/${codeSelectedGroup}/items/${codeId}`, { method: 'DELETE' });
        closeModal('modal-delete-item');
        await reloadItems();
        await reloadGroups();
    } catch (e) { alert(e.message); }
}

// ── init ─────────────────────────────────────────────────────────
bindGroupHover();

// 기능이 있는 모달(등록·수정·삭제)은 backdrop 클릭으로 닫히지 않음
// 단순 정보 확인 전용 모달에만 backdrop 클릭 닫기를 적용할 것
