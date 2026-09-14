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

    Loading.begin();
    try {
        const res = await fetch(url, { ...opts, headers });
        if (!res.ok) throw new Error(await errorMessage(res));
        return res.status === 204 ? null : await res.json();
    } finally {
        Loading.end();
    }
}

/** 서버 메시지를 못 쓸 때 보여줄 일반 문구 */
const GENERIC_ERROR_MESSAGE = '요청을 처리하는 중 오류가 발생했습니다.';

/**
 * 오류 응답에서 사용자에게 보여줄 메시지만 뽑는다.
 * 공통 AjaxResponse({ success:false, message }) 의 message 만 신뢰한다.
 * 스프링 기본 /error 응답이나 비 JSON 본문(HTML 오류페이지·스택트레이스·SQL 등)은
 * 내부 정보가 섞일 수 있으므로 절대 그대로 노출하지 않고 일반 문구로 대체한다.
 * @param {Response} res
 * @returns {Promise<string>}
 */
async function errorMessage(res) {
    const body = await res.text().catch(() => '');
    try {
        const parsed = JSON.parse(body);
        if (parsed && parsed.success === false && typeof parsed.message === 'string' && parsed.message.trim()) {
            return parsed.message;
        }
    } catch { /* JSON 아님 — 아래 일반 문구로 */ }
    return GENERIC_ERROR_MESSAGE;
}
