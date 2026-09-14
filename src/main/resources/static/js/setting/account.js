/**
 * 설정 화면 계좌·카드 관리.
 * templates/setting/index.html 에서 사용한다.
 *
 * htmx는 화면 전환 시 이 조각의 스크립트를 전역에서 다시 실행하므로 IIFE로 감싼다.
 * 렌더 결과의 inline onclick에서 참조하는 핸들러만 window(AccountSettings)로 노출한다.
 */
(function () {
    'use strict';

    // ACCOUNT_TYPE_CD -> 아이콘·색 자동 배정. 사용자가 색을 고르지 않는 것이 디자인 토큰 정책이다.
    // 카드=blush(지출·부채), 입출금=sage, 저축=sky(차분), 현금=중립. blush를 삭제/경고에만 쓰는 이 화면 관례와 맞춘다.
    const TYPE_STYLE = {
        ACCOUNTTYPE_001: { badge: 'bg-blush-light text-blush',     icon: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>' },        // 카드
        ACCOUNTTYPE_002: { badge: 'bg-sage-light text-sage',       icon: '<path d="M3 21h18"/><path d="M5 21V10l7-5 7 5v11"/><path d="M9 21v-6h6v6"/>' }, // 입출금
        ACCOUNTTYPE_003: { badge: 'bg-sky-light text-sky',         icon: '<circle cx="12" cy="12" r="9"/><path d="M12 7v10"/><path d="M7 12h10"/>' },     // 저축
        ACCOUNTTYPE_004: { badge: 'bg-cream-subtle text-ink-soft', icon: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2.5"/>' }, // 현금
    };
    const DEFAULT_STYLE = { badge: 'bg-cream-subtle text-ink-soft', icon: '<circle cx="12" cy="12" r="9"/>' };

    let accounts = [];
    let editingId = null;
    let deletingId = null;

    /** 계좌·카드 목록을 현재 데이터로 다시 그린다 */
    function renderAccounts() {
        const box = document.getElementById('account-list');
        if (accounts.length === 0) {
            box.innerHTML = `<p class="px-7 py-10 text-center text-sm text-ink-muted">등록된 계좌·카드가 없어요</p>`;
            return;
        }

        box.innerHTML = accounts.map((a, i) => {
            const border = i < accounts.length - 1 ? 'border-b border-cream-border' : '';
            const style = TYPE_STYLE[a.accountTypeCd] || DEFAULT_STYLE;
            const meta = [a.accountTypeNm, a.issuerNm].filter(Boolean).map(escapeHtml).join(' · ');
            return `
                <div class="flex items-center gap-4 px-7 py-4 ${border}">
                    <span class="w-9 h-9 rounded-xl shrink-0 flex items-center justify-center ${style.badge}">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${style.icon}</svg>
                    </span>
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-ink truncate">${escapeHtml(a.accountNm)}</p>
                        <p class="text-xs text-ink-muted mt-0.5">${meta || '-'}</p>
                    </div>
                    <div class="flex items-center gap-1.5 shrink-0">
                        <button type="button" title="수정" onclick="AccountSettings.edit(${a.accountId})"
                                class="inline-flex items-center justify-center w-7 h-7 rounded-lg bg-cream-surface text-ink-soft border border-cream-border hover:border-sage hover:text-sage transition-colors">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z"/></svg>
                        </button>
                        <button type="button" title="삭제" onclick="AccountSettings.confirmDelete(${a.accountId})"
                                class="inline-flex items-center justify-center w-7 h-7 rounded-lg bg-cream-surface text-ink-soft border border-cream-border hover:border-blush hover:text-blush transition-colors">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18m-2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </div>
                </div>`;
        }).join('');
    }

    /** 목록을 서버 최신 데이터로 다시 그린다. 조회는 화면에 바로 드러나므로 성공 토스트는 띄우지 않는다. */
    async function loadAccounts() {
        try {
            accounts = await requestJson('/account/data');
            renderAccounts();
        } catch (e) {
            Toast.error('에러가 발생하였습니다.', e.message);
        }
    }

    /** 추가 모달을 초기 상태로 열고 편집 대상을 비운다 */
    function openAddModal() {
        editingId = null;
        document.getElementById('account-modal-title').textContent = '계좌·카드 추가';
        document.getElementById('btn-submit-account').textContent = '추가하기';
        Dropdown.reset('account-type-dd');
        document.getElementById('account-nm').value = '';
        document.getElementById('account-issuer').value = '';
        document.getElementById('account-memo').value = '';
        Modal.open('account-modal');
    }

    /** id에 해당하는 계좌 값을 채워 수정 모달을 연다 */
    function openEditModal(id) {
        const account = accounts.find(a => a.accountId === id);
        if (!account) return;

        editingId = id;
        document.getElementById('account-modal-title').textContent = '계좌·카드 수정';
        document.getElementById('btn-submit-account').textContent = '저장하기';
        Dropdown.select('account-type-dd', account.accountTypeCd || '', account.accountTypeNm || '선택');
        document.getElementById('account-nm').value = account.accountNm || '';
        document.getElementById('account-issuer').value = account.issuerNm || '';
        document.getElementById('account-memo').value = account.memo || '';
        Modal.open('account-modal');
    }

    /** 계좌 모달과 유형 드롭다운 패널을 닫는다 */
    function closeModal() {
        Modal.close('account-modal');
        document.getElementById('account-type-dd-dd')?.classList.add('hidden');
    }

    /** 입력값을 검증하고 편집 여부에 따라 계좌를 저장한 뒤 목록을 갱신한다 */
    async function submitAccount() {
        const accountTypeCd = Dropdown.getValue('account-type-dd');
        const accountNm = document.getElementById('account-nm').value.trim();
        const issuerNm = document.getElementById('account-issuer').value.trim();
        const memo = document.getElementById('account-memo').value.trim();

        if (!accountTypeCd || !accountNm) { Toast.error('유형과 이름을 입력해주세요.'); return; }

        const body = JSON.stringify({ accountTypeCd, accountNm, issuerNm, memo });
        try {
            if (editingId) {
                await requestJson(`/account/${editingId}`, { method: 'PUT', body });
                Toast.success('수정되었습니다.');
            } else {
                await requestJson('/account', { method: 'POST', body });
                Toast.success('추가되었습니다.');
            }
            closeModal();
            await loadAccounts();
        } catch (e) {
            Toast.error('에러가 발생하였습니다.', e.message);
        }
    }

    /** 삭제할 계좌명을 표시하고 삭제 확인 모달을 연다 */
    function confirmDelete(id) {
        const account = accounts.find(a => a.accountId === id);
        if (!account) return;

        deletingId = id;
        document.getElementById('account-delete-name').textContent = account.accountNm || '';
        Modal.open('account-delete-modal');
    }

    /** 삭제 확인 모달을 닫고 삭제 대상을 비운다 */
    function closeDeleteModal() {
        deletingId = null;
        Modal.close('account-delete-modal');
    }

    /** 삭제 확인 시 계좌를 삭제하고 목록을 갱신한다 */
    async function deleteAccount() {
        if (!deletingId) return;

        try {
            await requestJson(`/account/${deletingId}`, { method: 'DELETE' });
            closeDeleteModal();
            Toast.delete('삭제되었습니다.');
            await loadAccounts();
        } catch (e) {
            Toast.error('에러가 발생하였습니다.', e.message);
        }
    }

    document.getElementById('btn-open-account-modal').addEventListener('click', openAddModal);
    document.getElementById('btn-close-account-modal').addEventListener('click', closeModal);
    document.getElementById('btn-cancel-account').addEventListener('click', closeModal);
    document.getElementById('btn-submit-account').addEventListener('click', submitAccount);
    document.getElementById('btn-cancel-delete-account').addEventListener('click', closeDeleteModal);
    document.getElementById('btn-confirm-delete-account').addEventListener('click', deleteAccount);

    window.AccountSettings = { edit: openEditModal, confirmDelete };

    loadAccounts();
}());
