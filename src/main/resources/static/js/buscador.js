document.addEventListener('DOMContentLoaded', () => {

    const normalizar = (texto) => {
        if (!texto) return '';
        return texto.toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();
    };

    // =========================================================================
    // MÓDULO 1: BUSCADOR DE PRODUCTOS
    // =========================================================================
    const buscadorProductos = document.getElementById('buscadorProductos');
    if (buscadorProductos) {
        const tbodys = {
            'llantas': document.getElementById('tbody-llantas'),
            'marcos': document.getElementById('tbody-marcos'),
            'transmision': document.getElementById('tbody-transmision'),
            'bicicletas': document.getElementById('tbody-bicicletas'),
            'frenos': document.getElementById('tbody-frenos'),
            'suspension': document.getElementById('tbody-suspension'),
            'sillines': document.getElementById('tbody-sillines'),
            'otros': document.getElementById('tbody-otros')
        };

        let listaProductosGlobal = [];

        async function cargarProductosIniciales() {
            try {
                const response = await fetch('/api/buscar/productos');
                if (!response.ok) throw new Error('Error al obtener productos');
                listaProductosGlobal = await response.json();
                actualizarTablas(listaProductosGlobal); 
            } catch (error) {
                console.error('Error cargando productos:', error);
            }
        }

        cargarProductosIniciales();

        let temporizador;
        buscadorProductos.addEventListener('input', (evento) => {
            const termino = evento.target.value;
            clearTimeout(temporizador);
            temporizador = setTimeout(() => {
                filtrarYActualizarTablas(termino);
            }, 200);
        });

        function filtrarYActualizarTablas(termino) {
            const terminoNorm = normalizar(termino);
            const palabrasBusqueda = terminoNorm.split(/\s+/).filter(p => p.length > 0);

            const productosFiltrados = listaProductosGlobal.filter(p => {
                if (palabrasBusqueda.length === 0) return true;

                const nombreProv = (p.proveedor && typeof p.proveedor === 'object') ? p.proveedor.nombre : (p.proveedor || '');
                const textoBusqueda = normalizar(`
                    ${p.nombre || ''} 
                    ${p.marca || ''} 
                    ${p.categoria || ''} 
                    ${p.tipo || ''} 
                    ${p.condicion || ''} 
                    ${nombreProv}
                    ${p.codigoBarras || ''}
                `);

                return palabrasBusqueda.every(palabra => textoBusqueda.includes(palabra));
            });

            actualizarTablas(productosFiltrados);
        }

        function actualizarTablas(productos) {
            Object.values(tbodys).forEach(tbody => {
                if (tbody) tbody.innerHTML = '';
            });

            productos.forEach(p => {
                const cat = p.categoria ? p.categoria.toLowerCase() : 'otros';
                const tbody = tbodys[cat] || tbodys['otros'];
                
                if (tbody) {
                    tbody.insertAdjacentHTML('beforeend', generarFilaHTML(p, cat));
                }
            });
        }

        function generarFilaHTML(p, cat) {
            const imgSrc = (p.imagen && p.imagen.trim() !== '') 
                ? `/recursos/${p.imagen}` 
                : 'https://via.placeholder.com/80x80.png?text=Sin+Foto';
            
            const badgeClass = (p.stock < 5) ? 'bg-danger' : 'bg-info text-dark';
            const extraInfo = (cat === 'llantas' && p.tipo) ? `| Comp: <span>${p.tipo}</span>` : '';
            
            const nombreProveedor = (p.proveedor && typeof p.proveedor === 'object') 
                ? (p.proveedor.nombre || '') 
                : (p.proveedor || '');

            const codigoBarrasText = (p.codigoBarras && p.codigoBarras.trim() !== '') ? p.codigoBarras : 'N/A';

            return `
                <tr>
                    <td class="ps-4">
                        <img src="${imgSrc}" class="catalogo-img rounded" alt="Foto">
                    </td>
                    <td class="fw-bold text-muted">ID-${p.idProducto}</td>
                    <td>
                        <span class="fw-bold text-dark">${p.nombre || 'Sin nombre'}</span><br>
                        <small class="text-muted">
                            <i class="bi bi-tag-fill me-1"></i><span>${p.marca || 'Genérica'}</span> ${extraInfo}
                        </small>
                    </td>
                    <td>${p.condicion || ''}</td>
                    <td>${nombreProveedor}</td>
                    <td><span class="text-muted">${codigoBarrasText}</span></td>
                    <td class="text-end fw-bold" style="color: var(--brand-blue);">$ ${p.precio || 0}</td>
                    <td class="text-center">
                        <span class="badge ${badgeClass}">${p.stock || 0} und</span>
                    </td>
                    <td class="text-center">
                        <div class="btn-group">
                            <a href="/admin/producto/editar/${p.idProducto}" class="btn btn-sm btn-outline-primary" title="Editar">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <a href="/admin/producto/eliminar/${p.idProducto}" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar este producto?');" title="Eliminar">
                                <i class="bi bi-trash"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            `;
        }
    }


    // =========================================================================
    // MÓDULO 2: BUSCADOR DE PROVEEDORES
    // =========================================================================
    const buscadorProveedores = document.getElementById('buscadorProveedores');
    if (buscadorProveedores) {
        const tbodyProveedores = document.getElementById('tbody-proveedores');
        let listaProveedoresGlobal = [];

        async function cargarProveedoresIniciales() {
            try {
                const response = await fetch('/api/buscar/proveedores');
                if (!response.ok) throw new Error('Error al obtener proveedores');
                listaProveedoresGlobal = await response.json();
                actualizarTablaProveedores(listaProveedoresGlobal); 
            } catch (error) {
                console.error('Error cargando proveedores:', error);
            }
        }

        cargarProveedoresIniciales();

        let temporizadorProv;
        buscadorProveedores.addEventListener('input', (evento) => {
            const termino = evento.target.value;
            clearTimeout(temporizadorProv);
            temporizadorProv = setTimeout(() => {
                filtrarYActualizarProveedores(termino);
            }, 200);
        });

        function filtrarYActualizarProveedores(termino) {
            const terminoNorm = normalizar(termino);
            const palabrasBusqueda = terminoNorm.split(/\s+/).filter(p => p.length > 0);

            const proveedoresFiltrados = listaProveedoresGlobal.filter(prov => {
                if (palabrasBusqueda.length === 0) return true;

                const textoBusqueda = normalizar(`
                    ${prov.nombre || ''} 
                    ${prov.tipoEstablecimiento || ''} 
                    ${prov.numero || ''} 
                    ${prov.correo || ''} 
                    ${prov.direccion || ''}
                `);

                return palabrasBusqueda.every(palabra => textoBusqueda.includes(palabra));
            });

            actualizarTablaProveedores(proveedoresFiltrados);
        }

        function actualizarTablaProveedores(proveedores) {
            if (!tbodyProveedores) return;
            tbodyProveedores.innerHTML = '';

            if (proveedores.length === 0) {
                tbodyProveedores.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center py-5 text-muted">
                            <i class="bi bi-buildings fs-1 d-block mb-2 opacity-50"></i>
                            No se encontraron proveedores que coincidan con la búsqueda.
                        </td>
                    </tr>
                `;
                return;
            }

            proveedores.forEach(prov => {
                tbodyProveedores.insertAdjacentHTML('beforeend', generarFilaProveedorHTML(prov));
            });
        }

        function generarFilaProveedorHTML(prov) {
            return `
                <tr>
                    <td class="px-4 py-3 fw-semibold text-dark">${prov.nombre || ''}</td>
                    <td class="py-3">
                        <span class="badge bg-info bg-opacity-10 text-info border border-info rounded-pill px-3">
                            ${prov.tipoEstablecimiento || ''}
                        </span>
                    </td>
                    <td class="py-3">
                        <div class="d-flex flex-column">
                            <small class="text-dark"><i class="bi bi-telephone text-muted me-1"></i> ${prov.numero || ''}</small>
                            <small class="text-muted"><i class="bi bi-envelope me-1"></i> ${prov.correo || ''}</small>
                        </div>
                    </td>
                    <td class="py-3 text-muted">${prov.direccion || ''}</td>
                    <td class="px-4 py-3 text-end">
                        <div class="btn-group shadow-sm">
                            <a href="/admin/proveedor/editar/${prov.idProveedor}" class="btn btn-light btn-sm text-primary border" title="Editar">
                                <i class="bi bi-pencil-square"></i>
                            </a>
                            <a href="/admin/proveedor/eliminar/${prov.idProveedor}" class="btn btn-light btn-sm text-danger border" title="Eliminar" onclick="return confirm('¿Estás seguro de eliminar este proveedor?');">
                                <i class="bi bi-trash"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            `;
        }
    }

});