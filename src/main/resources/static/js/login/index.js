/**
 * 로그인 화면 스크립트.
 * templates/login/index.html 에서 사용한다.
 */
function bindPasswordToggle() {
    const btn = document.getElementById('toggle-pw');
    const pw  = document.getElementById('password');
    if (!btn || !pw) return;
    btn.addEventListener('click', () => {
        pw.type = pw.type === 'password' ? 'text' : 'password';
    });
}

document.addEventListener('DOMContentLoaded', bindPasswordToggle);
