document.addEventListener('DOMContentLoaded', function() {
    const buscador = document.getElementById('buscadorProductos');
    
    if (!buscador) return; 

    let temporizador;

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

    buscador.addEventListener('input', function() {
        const termino = this.value;
        
        clearTimeout(temporizador);
        temporizador = setTimeout(() => {
            buscarEnServidor(termino);
        }, 300);
    });

    function buscarEnServidor(termino) {
        fetch(`/api/buscar/productos?termino=${encodeURIComponent(termino)}`)
            .then(response => response.json())
            .then(productos => actualizarTablas(productos))
            .catch(error => console.error('Error en la búsqueda:', error));
    }

    // Función para limpiar tildes y evitar fallos de categoría
    function normalizarCategoria(cat) {
        if (!cat) return 'otros';
        const c = cat.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim();
        if (c.includes('llanta')) return 'llantas';
        if (c.includes('marco') || c.includes('cuadro')) return 'marcos';
        if (c.includes('transmision') || c.includes('arrastre')) return 'transmision';
        if (c.includes('bicicleta')) return 'bicicletas';
        if (c.includes('freno')) return 'frenos';
        if (c.includes('suspension') || c.includes('horquilla')) return 'suspension';
        if (c.includes('sillin') || c.includes('tija')) return 'sillines';
        return 'otros';
    }

    function actualizarTablas(productos) {
        Object.values(tbodys).forEach(tbody => {
            if(tbody) tbody.innerHTML = '';
        });

        productos.forEach(p => {
            const catKey = normalizarCategoria(p.categoria);
            const tbody = tbodys[catKey];
            
            if (tbody) {
                const tr = document.createElement('tr');
                
                const imgSrc = (p.imagen && p.imagen.trim() !== '') 
                    ? '/recursos/' + p.imagen 
                    : 'https://via.placeholder.com/80x80.png?text=Sin+Foto';
                
                const badgeClass = (p.stock < 5) ? 'bg-danger' : 'bg-info text-dark';
                let extraInfo = (catKey === 'llantas') ? `| Comp: <span>${p.tipo || ''}</span>` : '';

                tr.innerHTML = `
                    <td class="ps-4"><img src="${imgSrc}" class="catalogo-img rounded" alt="Foto"></td>
                    <td class="fw-bold text-muted">ID-${p.idProducto}</td>
                    <td>
                        <span class="fw-bold text-dark">${p.nombre}</span><br>
                        <small class="text-muted"><i class="bi bi-tag-fill me-1"></i><span>${p.marca}</span> ${extraInfo}</small>
                    </td>
                    <td>${p.condicion || ''}</td>
                    <td>${p.proveedor || ''}</td>
                    <td class="text-end fw-bold" style="color: var(--brand-blue);">$ ${p.precio}</td>
                    <td class="text-center"><span class="badge ${badgeClass}">${p.stock} und</span></td>
                    <td class="text-center">
                        <div class="btn-group">
                            <a href="/admin/producto/editar/${p.idProducto}" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a>
                            <a href="/admin/producto/eliminar/${p.idProducto}" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar este producto?');" title="Eliminar"><i class="bi bi-trash"></i></a>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            }
        });
    }
});