/**
 * 회원가입 화면 스크립트.
 * templates/user/join.html 에서 사용한다.
 */

// ── 비밀번호 토글
document.querySelectorAll('[data-toggle]').forEach(btn => {
    btn.addEventListener('click', () => {
        const target = document.getElementById(btn.dataset.toggle);
        if (target) target.type = target.type === 'password' ? 'text' : 'password';
    });
});

// ── 아이디 글자수
const loginId = document.getElementById('loginId');
const loginIdCount = document.getElementById('loginId-count');
let loginIdChecked = false;
loginId.addEventListener('input', () => {
    loginIdCount.textContent = loginId.value.length;
    loginIdChecked = false;
});

// ── 비밀번호 강도
const passwd = document.getElementById('passwd');
const confirm = document.getElementById('passwd-confirm');
const segs = document.querySelectorAll('.strength-seg');
const strengthLabel = document.getElementById('strength-label');
const confirmMsg = document.getElementById('confirm-msg');

const STRENGTH = [
    { label: '비밀번호를 입력해주세요.', color: '#E5E0D8', text: '#ADA89F' },
    { label: '약함 — 더 길고 다양하게', color: 'oklch(0.62 0.12 22)', text: 'oklch(0.62 0.12 22)' },
    { label: '보통 — 조금 더 강화해볼까요?', color: 'rgb(220, 163, 47)', text: 'rgb(220, 163, 47)' },
    { label: '좋음 — 안전한 비밀번호예요', color: 'oklch(0.78 0.07 152)', text: 'oklch(0.58 0.1 152)' },
    { label: '매우 강함 — 완벽해요 ✓', color: 'oklch(0.58 0.1 152)', text: 'oklch(0.58 0.1 152)' },
];

function calcStrength(v) {
    if (!v) return 0;
    let score = 0;
    if (v.length >= 8) score++;
    if (/[A-Z]/.test(v) && /[a-z]/.test(v)) score++;
    if (/\d/.test(v)) score++;
    if (/[^A-Za-z0-9]/.test(v)) score++;
    return Math.min(score, 4);
}

passwd.addEventListener('input', () => {
    const score = calcStrength(passwd.value);
    const s = STRENGTH[score];
    segs.forEach((seg, i) => {
        seg.style.background = i < score ? s.color : '#E5E0D8';
    });
    strengthLabel.textContent = s.label;
    strengthLabel.style.color = s.text;
    checkConfirm();
});

// ── 비밀번호 확인
function checkConfirm() {
    if (!confirm.value) {
        confirmMsg.textContent = ' ';
        confirmMsg.style.color = '';
        return;
    }
    if (confirm.value === passwd.value) {
        confirmMsg.textContent = '비밀번호가 일치해요.';
        confirmMsg.style.color = 'oklch(0.58 0.1 152)';
    } else {
        confirmMsg.textContent = '비밀번호가 일치하지 않아요.';
        confirmMsg.style.color = 'oklch(0.62 0.12 22)';
    }
}
confirm.addEventListener('input', checkConfirm);

// ── 아이디 중복확인
const loginIdMsg = document.getElementById('loginId-msg');
document.getElementById('check-loginId').addEventListener('click', async () => {
    const v = loginId.value.trim();
    if (!v) {
        loginIdMsg.textContent = '아이디를 먼저 입력해주세요.';
        loginIdMsg.style.color = 'oklch(0.62 0.12 22)';
        return;
    }
    if (!/^[A-Za-z0-9]{4,20}$/.test(v)) {
        loginIdMsg.textContent = '영문, 숫자만 사용해 4~20자로 입력해주세요.';
        loginIdMsg.style.color = 'oklch(0.62 0.12 22)';
        return;
    }
    try {
        const res = await fetch(`/user/check-loginid?loginId=${encodeURIComponent(v)}`);
        if (!res.ok) throw new Error('중복확인 요청이 실패했어요.');
        const available = await res.json();
        if (available) {
            loginIdChecked = true;
            loginIdMsg.textContent = '사용 가능한 아이디예요. ✓';
            loginIdMsg.style.color = 'oklch(0.58 0.1 152)';
        } else {
            loginIdChecked = false;
            loginIdMsg.textContent = '이미 사용 중인 아이디예요.';
            loginIdMsg.style.color = 'oklch(0.62 0.12 22)';
        }
    } catch (err) {
        loginIdChecked = false;
        loginIdMsg.textContent = '중복확인 중 오류가 발생했어요. 다시 시도해주세요.';
        loginIdMsg.style.color = 'oklch(0.62 0.12 22)';
    }
});

// ── 폼 제출 검증
document.getElementById('signup-form').addEventListener('submit', e => {
    if (!loginIdChecked) {
        e.preventDefault();
        loginIdMsg.textContent = '아이디 중복확인을 해주세요.';
        loginIdMsg.style.color = 'oklch(0.62 0.12 22)';
        loginId.focus();
    }
});

// ── 전체 동의
const agreeAll = document.getElementById('agree-all');
const agreeItems = document.querySelectorAll('.agree-item');
agreeAll.addEventListener('change', () => {
    agreeItems.forEach(i => { i.checked = agreeAll.checked; });
});
agreeItems.forEach(i => i.addEventListener('change', () => {
    agreeAll.checked = [...agreeItems].every(x => x.checked);
}));
