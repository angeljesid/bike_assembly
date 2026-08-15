document.addEventListener('DOMContentLoaded', () => {
    const buscador = document.getElementById('buscadorProductos');
    if (!buscador) return;

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

    let temporizador;

    buscador.addEventListener('input', (evento) => {
        const termino = evento.target.value;
        
        clearTimeout(temporizador);
        temporizador = setTimeout(() => {
            buscarEnServidor(termino);
        }, 300);
    });

    async function buscarEnServidor(termino) {
        try {
            const url = `/api/buscar/productos?termino=${encodeURIComponent(termino)}`;
            const response = await fetch(url);
            
            if (!response.ok) throw new Error('Error en la respuesta del servidor');
            
            const productos = await response.json();
            actualizarTablas(productos);
        } catch (error) {
            console.error('Fallo en la búsqueda de productos:', error);
        }
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
                <td>${p.proveedor || ''}</td>
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
});