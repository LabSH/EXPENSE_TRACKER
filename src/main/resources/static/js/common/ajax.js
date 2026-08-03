/**
 * 공통 Ajax 유틸리티
 * - 여러 화면(admin/code, admin/users, admin/logs 등)에서 동일하게 구현되어 있던
 *   CSRF 토큰 조회 + fetch 래퍼를 하나로 모은다.
 * - 서버 Ajax 응답은 현재 원본 JSON을 그대로 내려주므로, 이 함수도 원본 JSON을 그대로 반환한다.
 */

/**
 * 쿠키에서 CSRF 토큰(XSRF-TOKEN)을 읽어온다.
 * @returns {string}
 */
function getCsrfToken() {
    const raw = document.cookie.split('; ').find(r => r.startsWith('XSRF-TOKEN='))?.split('=')[1] ?? '';
    return decodeURIComponent(raw);
}

/**
 * JSON 기반 fetch 래퍼.
 * - GET이 아닌 요청에는 CSRF 토큰을 자동으로 첨부한다.
 * - HTTP 오류(res.ok === false)는 서버 응답 본문을 메시지로 담은 Error를 던진다.
 * - 204 No Content는 null을 반환한다.
 * @param {string} url
 * @param {RequestInit} [opts]
 * @returns {Promise<any>}
 */
async function requestJson(url, opts = {}) {
    const method = (opts.method || 'GET').toUpperCase();
    const headers = { 'Content-Type': 'application/json', ...opts.headers };
    if (method !== 'GET') headers['X-XSRF-TOKEN'] = getCsrfToken();

    const res = await fetch(url, { ...opts, headers });
    if (!res.ok) {
        const msg = await res.text().catch(() => res.statusText);
        throw new Error(msg || res.statusText);
    }
    return res.status === 204 ? null : res.json();
}
