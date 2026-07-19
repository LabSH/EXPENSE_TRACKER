const Dropdown = (() => {

    function toggle(id) {
        document.getElementById(id + '-dd').classList.toggle('hidden');
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
            const wrap = document.getElementById(dd.id.replace(/-dd$/, '-wrap'));
            if (wrap && !wrap.contains(e.target)) dd.classList.add('hidden');
        });
    });

    return { toggle, select, getValue, reset };
})();
