/**
 * 500 에러 화면 스크립트.
 * templates/error/500.html 에서 사용한다.
 */
function bindReloadButton() {
    const btn = document.getElementById('btn-reload');
    if (!btn) return;
    btn.addEventListener('click', () => location.reload());
}

document.addEventListener('DOMContentLoaded', bindReloadButton);
