/**
 * 공통 첨부파일 유틸리티
 * - 서버 다운로드 엔드포인트(GET /files/{fileGroupId}/{fileId}/download) 호출을 한곳에 모은다.
 * - 목록 행 요소에 data-fgid / data-fid / data-fname 를 심어두면 bindList 로 클릭-다운로드를 위임 바인딩할 수 있다.
 * - 의존: js/common/ajax.js(errorMessage), js/common/loading.js(Loading), js/common/toast.js(Toast)
 */
(function () {
    'use strict';

    /**
     * 첨부파일을 blob 으로 받아 내려받는다. 실패 시 공통 토스트로 안내한다.
     * @param {string} fileGroupId
     * @param {string} fileId
     * @param {string} [fileName] 저장 파일명. 없으면 'download'
     */
    async function download(fileGroupId, fileId, fileName) {
        Loading.begin();
        try {
            const res = await fetch(`/files/${encodeURIComponent(fileGroupId)}/${encodeURIComponent(fileId)}/download`);
            if (!res.ok) throw new Error(await errorMessage(res));
            const blobUrl = URL.createObjectURL(await res.blob());
            const a = document.createElement('a');
            a.href = blobUrl;
            a.download = fileName || 'download';
            document.body.appendChild(a);
            a.click();
            a.remove();
            // 일부 브라우저에서 click 직후 동기 revoke 시 다운로드가 잘릴 수 있어 한 틱 미룬다
            setTimeout(() => URL.revokeObjectURL(blobUrl), 0);
        } catch (e) {
            Toast.error('에러가 발생하였습니다.', e.message);
        } finally {
            Loading.end();
        }
    }

    /**
     * 컨테이너에 클릭 위임을 걸어, data-fid 를 가진 행을 누르면 다운로드한다.
     * 컨테이너가 innerHTML 로 다시 그려져도 위임이라 재바인딩이 필요 없다.
     * @param {HTMLElement|null} container
     */
    function bindList(container) {
        if (!container) return;
        container.addEventListener('click', e => {
            const el = e.target.closest('[data-fid]');
            if (el) download(el.dataset.fgid, el.dataset.fid, el.dataset.fname);
        });
    }

    window.AttachFile = { download, bindList };
}());
