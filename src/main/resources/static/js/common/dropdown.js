const Dropdown = (() => {

    /** 열림 상태에 맞춰 화살표(chevron)를 위/아래로 회전시킨다 */
    function setChevron(id, open) {
        const chevron = document.getElementById(id + '-chevron');
        if (chevron) chevron.style.transform = open ? 'rotate(180deg)' : '';
    }

    /** 드롭다운을 닫고 화살표를 원위치시킨다 */
    function close(id) {
        document.getElementById(id + '-dd')?.classList.add('hidden');
        setChevron(id, false);
    }

    function toggle(id) {
        const dd = document.getElementById(id + '-dd');
        if (dd.classList.contains('hidden')) {
            // 고를 옵션이 하나도 없으면 펼치지 않는다
            if (dd.querySelectorAll('button').length === 0) return;
            document.body.appendChild(dd); // 조상의 transform/overflow 영향을 받지 않도록 body 직속으로 이동
            const rect = document.getElementById(id + '-btn').getBoundingClientRect();
            dd.style.position = 'fixed';
            dd.style.top  = `${rect.bottom + 6}px`;
            dd.style.left = `${rect.left}px`;
            dd.classList.remove('hidden');
            setChevron(id, true);
        } else {
            close(id);
        }
    }

    function select(id, code, name) {
        document.getElementById(id + '-label').textContent = name;
        document.getElementById(id + '-wrap').dataset.value = code;
        close(id);
    }

    function getValue(id) {
        return document.getElementById(id + '-wrap')?.dataset.value ?? '';
    }

    function reset(id) {
        const allBtn = document.querySelector(`#${id}-dd [data-code=""]`);
        if (allBtn) {
            select(id, '', allBtn.dataset.name);
            return;
        }
        // '전체' 옵션이 없는 입력용 드롭다운은 placeholder 로 라벨을 되돌린다
        const wrap = document.getElementById(id + '-wrap');
        select(id, '', wrap ? wrap.dataset.placeholder ?? '' : '');
    }

    document.addEventListener('mousedown', e => {
        document.querySelectorAll('[id$="-dd"]').forEach(dd => {
            const baseId = dd.id.replace(/-dd$/, '');
            const wrap   = document.getElementById(baseId + '-wrap');
            const inside = dd.contains(e.target) || (wrap && wrap.contains(e.target));
            if (!inside) close(baseId);
        });
    });

    return { toggle, select, getValue, reset };
})();
