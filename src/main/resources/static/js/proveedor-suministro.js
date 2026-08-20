document.addEventListener('DOMContentLoaded', () => {
    const proveedorId = document.getElementById('proveedorIdHidden')?.value;
    if(!proveedorId) return;

    const pIni = document.getElementById('formPrecioI');
    const pIva = document.getElementById('formIva');
    const pTot = document.getElementById('formPrecioT');
    const calc = () => {
        const i = parseFloat(pIni.value)||0;
        const v = parseFloat(pIva.value)||0;
        pTot.value = (i + (i * (v/100))).toFixed(2);
    };
    if(pIni) { pIni.addEventListener('input', calc); pIva.addEventListener('input', calc); }

    const buscador = document.getElementById('buscadorSuministros');
    const alerta = document.getElementById('alertaSuministros');
    const resultados = document.getElementById('resultadosSuministros');
    const btnMostrarNuevo = document.getElementById('btnMostrarNuevo');
    const formNuevo = document.getElementById('formNuevoProducto');
    
    let catalogo = [];

    btnMostrarNuevo.addEventListener('click', () => {
        formNuevo.style.display = formNuevo.style.display === 'none' ? 'block' : 'none';
        if(formNuevo.style.display === 'block') formNuevo.scrollIntoView({behavior: "smooth"});
    });

    async function cargarCatalogoGlobal() {
        try {
            const res = await fetch('/api/buscar/productos');
            if(res.ok) catalogo = await res.json();
        } catch(e) { console.error('Error cargando catalogo', e); }
    }
    cargarCatalogoGlobal();

    const normalizar = (txt) => txt ? txt.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim() : '';

    let timeoutBusqueda;
    buscador.addEventListener('input', (e) => {
        const termino = e.target.value;
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            const norm = normalizar(termino);
            if(norm.length < 2) {
                resultados.innerHTML = '';
                alerta.style.display = 'none';
                return;
            }

            const palabras = norm.split(/\s+/);
            const filtrados = catalogo.filter(p => {
                const textoBusq = normalizar(`${p.nombre || ''} ${p.marca || ''} ${p.categoria || ''} ${p.codigoBarras || ''}`);
                return palabras.every(pal => textoBusq.includes(pal));
            });

            if(filtrados.length > 0) {
                alerta.style.display = 'block';
                let html = '';
                const basesUnicas = Array.from(new Set(filtrados.map(p => p.nombre)))
                    .map(nombre => filtrados.find(p => p.nombre === nombre));

                basesUnicas.forEach(p => {
                    html += `
                        <div class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 border-0 border-bottom">
                            <div class="d-flex align-items-center gap-3">
                                <div class="bg-light p-2 rounded border"><i class="bi bi-gear-wide-connected fs-4 text-secondary"></i></div>
                                <div>
                                    <h6 class="fw-bold mb-0 text-dark">${p.nombre}</h6>
                                    <small class="text-muted">Marca: ${p.marca || 'N/A'} | Código: ${p.codigoBarras || 'N/A'}</small>
                                </div>
                            </div>
                            <button class="btn btn-outline-success btn-sm fw-bold rounded-pill px-3 shadow-sm btn-abrir-modal" 
                                data-id="${p.idProducto}" data-nombre="${p.nombre}" data-marca="${p.marca}">
                                <i class="bi bi-plus-lg"></i> Sumar Ingreso
                            </button>
                        </div>
                    `;
                });
                resultados.innerHTML = html;
                asignarEventosModal();
            } else {
                alerta.style.display = 'none';
                resultados.innerHTML = `
                    <div class="text-center py-4 text-muted border rounded bg-light">
                        <i class="bi bi-emoji-frown fs-2 d-block mb-2"></i>
                        No hay componentes que coincidan. ¡Puedes crearlo con seguridad!
                    </div>`;
            }
        }, 300);
    });

    const modalComponent = new bootstrap.Modal(document.getElementById('modalSuministro'));
    const btnGuardarSum = document.getElementById('btnGuardarSuministro');

    function asignarEventosModal() {
        document.querySelectorAll('.btn-abrir-modal').forEach(btn => {
            btn.addEventListener('click', () => {
                document.getElementById('modalIdProducto').value = btn.dataset.id;
                document.getElementById('modalNombreProducto').innerText = btn.dataset.nombre;
                document.getElementById('modalSubProducto').innerText = `Marca base: ${btn.dataset.marca}`;
                document.getElementById('modalCantidad').value = 1;
                document.getElementById('modalCondicion').value = 'Nuevo';
                modalComponent.show();
            });
        });
    }

    btnGuardarSum.addEventListener('click', async () => {
        const idProd = document.getElementById('modalIdProducto').value;
        const cant = parseInt(document.getElementById('modalCantidad').value);
        const cond = document.getElementById('modalCondicion').value;
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const reqHeader = { 'Content-Type': 'application/json' };
        if (token && header) reqHeader[header] = token;

        btnGuardarSum.disabled = true;
        btnGuardarSum.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Guardando...';

        try {
            const response = await fetch('/admin/proveedor/api/suministro', {
                method: 'POST',
                headers: reqHeader,
                body: JSON.stringify({
                    idProducto: idProd,
                    idProveedor: proveedorId,
                    cantidad: cant,
                    condicion: cond
                })
            });
            const res = await response.json();
            if(res.success) {
                location.reload();
            } else {
                alert('Error al guardar: ' + res.msg);
                btnGuardarSum.disabled = false;
                btnGuardarSum.innerText = 'Confirmar Ingreso';
            }
        } catch(e) {
            console.error(e);
            alert('Fallo de red conectando al sistema.');
            btnGuardarSum.disabled = false;
            btnGuardarSum.innerText = 'Confirmar Ingreso';
        }
    });
});