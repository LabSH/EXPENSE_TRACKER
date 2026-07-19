const Dropdown = (() => {

    function toggle(id) {
        const dd = document.getElementById(id + '-dd');
        if (dd.classList.contains('hidden')) {
            document.body.appendChild(dd); // 조상의 transform/overflow 영향을 받지 않도록 body 직속으로 이동
            const rect = document.getElementById(id + '-btn').getBoundingClientRect();
            dd.style.position = 'fixed';
            dd.style.top  = `${rect.bottom + 6}px`;
            dd.style.left = `${rect.left}px`;
            dd.classList.remove('hidden');
        } else {
            dd.classList.add('hidden');
        }
    }

    function select(id, code, name) {
        document.getElementById(id + '-label').textContent = name;
        document.getElementById(id + '-dd').classList.add('hidden');
        document.getElementById(id + '-wrap').dataset.value = code;
    }

    function getValue(id) {
        return document.getElementById(id + '-wrap')?.dataset.value ?? '';
    }

    function reset(id) {
        const allBtn = document.querySelector(`#${id}-dd [data-code=""]`);
        if (allBtn) select(id, '', allBtn.dataset.name);
    }

    document.addEventListener('mousedown', e => {
        document.querySelectorAll('[id$="-dd"]').forEach(dd => {
            const wrap   = document.getElementById(dd.id.replace(/-dd$/, '-wrap'));
            const inside = dd.contains(e.target) || (wrap && wrap.contains(e.target));
            if (!inside) dd.classList.add('hidden');
        });
    });

    return { toggle, select, getValue, reset };
})();
