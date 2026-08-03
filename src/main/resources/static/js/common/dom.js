/**
 * 공통 DOM 유틸리티
 * - 여러 화면에서 반복되는 값 이스케이프, 필수 엘리먼트 조회 등을 모아둔다.
 */

/**
 * 사용자 입력/서버 데이터를 innerHTML에 삽입하기 전 HTML 특수문자를 이스케이프한다.
 * @param {*} value
 * @returns {string}
 */
function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/**
 * id로 엘리먼트를 조회하고, 없으면 콘솔에 경고를 남긴다.
 * @param {string} id
 * @returns {HTMLElement|null}
 */
function getRequiredElement(id) {
    const el = document.getElementById(id);
    if (!el) console.warn(`[dom] 엘리먼트를 찾을 수 없습니다: #${id}`);
    return el;
}
