let productosData = [];
let categoriaActiva = '';

/* Guardados ahora viven en el servidor (por usuario autenticado).
   Esta lista en memoria se sincroniza al navegar y tras cada guardar/quitar. */
let guardadosServidor = [];

document.addEventListener("DOMContentLoaded", () => {
    if (document.getElementById("contenedorProductos")) {
        cargarProductos();
    }
    cargarGuardadosIniciales();

    const buscador = document.getElementById("buscadorCliente");
    if(buscador) {
        buscador.addEventListener("keyup", (e) => {
            filtrarProductos(e.target.value);
        });
    }

    if (window.location.pathname.includes('/cliente/producto/')) {
        const partesRuta = window.location.pathname.split('/');
        const idProducto = partesRuta[partesRuta.length - 1];
        cargarDetalleProducto(idProducto);
    }
});

function guardadosIds() {
    return new Set(guardadosServidor.map(g => String(g.idProducto)));
}

/* Carga la lista de guardados del usuario autenticado desde el servidor.
   Al terminar, actualiza contra el contador y, si procede, la vista. */
async function cargarGuardadosIniciales() {
    try {
        const response = await fetch('/cliente/api/guardados');
        guardadosServidor = response.ok ? await response.json() : [];
    } catch (e) {
        console.error("Error al cargar guardados:", e);
        guardadosServidor = [];
    }
    actualizarContadorGuardados();
    if (window.location.pathname.includes('/cliente/guardados') || window.location.pathname.includes('/cliente/carrito')) {
        renderizarGuardados();
    }
    if (document.getElementById("contenedorProductos") && productosData.length) {
        filtrarProductos('');
    }
    actualizarEstadoBotonGuardar();
}

async function refrescarGuardados() {
    try {
        const response = await fetch('/cliente/api/guardados');
        guardadosServidor = response.ok ? await response.json() : [];
    } catch (e) {
        console.error("Error al refrescar guardados:", e);
    }
    actualizarContadorGuardados();
    const buscador = document.getElementById("buscadorCliente");
    const termino = buscador ? buscador.value : '';
    if (document.getElementById("contenedorProductos")) {
        filtrarProductos(termino);
    }
    actualizarEstadoBotonGuardar();
}

function datosCliente() {
    return {
        nombre: window.clienteNombre || '',
        cedula: window.clienteCedula || '',
        telefono: window.clienteTelefono || ''
    };
}

function mensajeIdentidad() {
    const c = datosCliente();
    let partes = [];
    if (c.nombre) {
        let quien = c.nombre;
        if (c.cedula) quien += ` (cédula ${c.cedula})`;
        partes.push(quien);
    }
    if (c.telefono) partes.push(`mi número es ${c.telefono}`);
    const quien = partes.length
        ? `Mi nombre es ${partes.join(', ')} y le escribo desde la tienda Bike Assembly.`
        : `Le escribo desde la tienda Bike Assembly.`;
    return quien;
}

function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(valor || 0);
}

async function cargarProductos() {
    try {
        const response = await fetch('/api/productos');
        if (!response.ok) throw new Error("Error en la respuesta del servidor");
        
        productosData = await response.json();
        
        const mensajeCarga = document.getElementById("mensajeCarga");
        const contenedor = document.getElementById("contenedorProductos");
        
        if (mensajeCarga) mensajeCarga.style.display = "none";
        if (contenedor) contenedor.style.display = "flex";
        
        renderizarCategorias();
        renderizarProductos(productosData);
    } catch (error) {
        console.error("Error al cargar productos:", error);
        const mensajeCarga = document.getElementById("mensajeCarga");
        if (mensajeCarga) {
            mensajeCarga.innerHTML = `<p class="text-danger fw-bold">Hubo un problema al cargar el catálogo. Intenta recargar la página.</p>`;
        }
    }
}

function renderizarProductos(productos) {
    const contenedor = document.getElementById("contenedorProductos");
    const msjSinResultados = document.getElementById("mensajeSinResultados");
    
    if (!contenedor) return;
    
    contenedor.innerHTML = "";

    if (productos.length === 0) {
        contenedor.style.display = "none";
        if (msjSinResultados) msjSinResultados.style.display = "block";
        return;
    }

    contenedor.style.display = "flex";
    if (msjSinResultados) msjSinResultados.style.display = "none";

    const idsGuardados = guardadosIds();

    productos.forEach(prod => {
        const precioAMostrar = prod.precioTotal ? prod.precioTotal : prod.precio;
        const precioFormateado = formatearMoneda(precioAMostrar);
        
        const imagen = (prod.imagen && prod.imagen !== 'null' && prod.imagen.trim() !== '') 
            ? `/recursos/${prod.imagen}` 
            : 'https://via.placeholder.com/80x80.png?text=Sin+Foto'; 
            
        const categoria = prod.categoria ? prod.categoria : 'General'; 
        const marca = prod.marca ? prod.marca : 'Sin marca';
        const condicion = prod.condicion ? prod.condicion : 'N/A';
        const nombreTaller = prod.nombreOrganizacion ? prod.nombreOrganizacion : 'Taller Independiente';

        const yaGuardado = idsGuardados.has(String(prod.idProducto));
        const iconoGuardar = yaGuardado ? 'bi-heart-fill text-danger' : 'bi-heart text-dark';

        const cardHTML = `
            <div class="col-12 col-sm-6 col-lg-4 col-xl-3">
                <div class="card product-card h-100 border-0 shadow-sm">
                    <a href="/cliente/producto/${prod.idProducto}" class="text-decoration-none text-dark">
                        <div class="img-container p-3 position-relative">
                            <span class="position-absolute top-0 start-0 m-2 badge bg-dark opacity-75"><i class="bi bi-shop me-1"></i> ${nombreTaller}</span>
                            <img src="${imagen}" alt="${prod.nombre}" onerror="this.src='https://via.placeholder.com/80x80.png?text=Sin+Foto'">
                        </div>
                    </a>
                    <div class="card-body d-flex flex-column border-top">
                        <span class="badge bg-light text-dark border mb-2 align-self-start">${categoria}</span>
                        <a href="/cliente/producto/${prod.idProducto}" class="text-decoration-none text-dark">
                            <h6 class="card-title fw-bold mb-1 text-truncate" title="${prod.nombre}">${prod.nombre}</h6>
                        </a>
                        <p class="card-text text-muted small mb-3">Marca: ${marca} <br> Condición: ${condicion}</p>
                        
                        <div class="mt-auto d-flex justify-content-between align-items-center">
                            <span class="fw-bold fs-5" style="color: var(--brand-blue);">${precioFormateado}</span>
                            <button class="btn btn-outline-primary btn-sm rounded-circle shadow-sm" style="width: 35px; height: 35px;" onclick='guardarProducto(${JSON.stringify(prod)})' title="Guardar para después">
                                <i class="bi ${iconoGuardar}" id="fav-${prod.idProducto}"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        contenedor.insertAdjacentHTML('beforeend', cardHTML);
    });
}

function filtrarProductos(termino) {
    const busqueda = termino.toLowerCase().trim();
    
    const filtrados = productosData.filter(prod => {
        if (categoriaActiva && prod.categoria !== categoriaActiva) {
            return false;
        }
        const nombreMatch = prod.nombre && prod.nombre.toLowerCase().includes(busqueda);
        const marcaMatch = prod.marca && prod.marca.toLowerCase().includes(busqueda);
        const categoriaMatch = prod.categoria && prod.categoria.toLowerCase().includes(busqueda);
        
        return nombreMatch || marcaMatch || categoriaMatch;
    });
    
    renderizarProductos(filtrados);
}

function renderizarCategorias() {
    const lista = document.getElementById("listaCategorias");
    const seccion = document.getElementById("seccionCategorias");
    if (!lista || !seccion) return;

    const categorias = [...new Set(
        productosData
            .map(p => p.categoria)
            .filter(c => c && c.trim() !== '')
            .map(c => c.trim())
    )].sort();

    if (categorias.length === 0) {
        seccion.style.display = "none";
        return;
    }

    seccion.style.display = "block";
    let html = `
        <button type="button" class="btn btn-sm rounded-pill ${categoriaActiva === '' ? 'btn-primary' : 'btn-outline-primary'}"
                onclick="filtrarPorCategoria('')">Todas</button>
    `;

    categorias.forEach(cat => {
        html += `
            <button type="button" class="btn btn-sm rounded-pill ${categoriaActiva === cat ? 'btn-primary' : 'btn-outline-primary'}"
                    onclick="filtrarPorCategoria('${escaparHtml(cat)}')">${escaparHtml(cat)}</button>
        `;
    });

    lista.innerHTML = html;
}

function filtrarPorCategoria(categoria) {
    categoriaActiva = categoria;
    renderizarCategorias();
    const buscador = document.getElementById("buscadorCliente");
    const termino = buscador ? buscador.value : '';
    filtrarProductos(termino);
}

function escaparHtml(valor) {
    return String(valor).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

async function cargarDetalleProducto(id) {
    try {
        const response = await fetch(`/api/productos`);
        if (!response.ok) throw new Error("No se pudo cargar la información");
        const productos = await response.json();
        const producto = productos.find(p => p.idProducto == id);

        if (producto) {
            const imgEl = document.getElementById("imgDetalle");
            const catEl = document.getElementById("categoriaDetalle");
            const nomEl = document.getElementById("nombreDetalle");
            const condEl = document.getElementById("condicionDetalle");
            const stockEl = document.getElementById("stockDetalle");
            const precEl = document.getElementById("precioDetalle");

            const imagen = (producto.imagen && producto.imagen !== 'null' && producto.imagen.trim() !== '') 
                ? `/recursos/${producto.imagen}` 
                : 'https://via.placeholder.com/80x80.png?text=Sin+Foto';

            if (imgEl) imgEl.src = imagen;
            if (catEl) catEl.innerText = producto.categoria || 'General';
            if (nomEl) nomEl.innerText = producto.nombre || 'Sin nombre';
            if (condEl) condEl.innerText = producto.condicion || 'N/A';
            if (stockEl) stockEl.innerText = (producto.disponibilidad || 0) + ' und';
            
            const precioFinal = producto.precioTotal ? producto.precioTotal : producto.precio;
            if (precEl) precEl.innerText = formatearMoneda(precioFinal);
            
            window.productoActualSeleccionado = producto;
            actualizarEstadoBotonGuardar();
        }
    } catch (e) {
        console.error("Error cargando detalle:", e);
    }
}

function actualizarEstadoBotonGuardar() {
    const prod = window.productoActualSeleccionado;
    if (!prod) return;
    const yaGuardado = guardadosIds().has(String(prod.idProducto));
    const btn = document.getElementById("btnGuardarDetalle");
    const icono = document.getElementById("iconoGuardarDetalle");
    if (btn) {
        btn.classList.toggle('btn-primary', yaGuardado);
        btn.classList.toggle('btn-outline-primary', !yaGuardado);
        const label = document.getElementById("labelGuardarDetalle");
        if (label) label.textContent = yaGuardado ? 'Guardado' : 'Guardar para después';
    }
    if (icono) {
        icono.className = yaGuardado ? 'bi bi-heart-fill me-2' : 'bi bi-heart me-2';
    }
}

function contactarDuenoDesdeDetalle() {
    const prod = window.productoActualSeleccionado;
    if (!prod) {
        alert('Aún no se carga la información del producto.');
        return;
    }
    contactarDueno(prod);
}

function contactarDueno(prod) {
    const telefonoTaller = prod.telefonoOrganizacion || '';
    const nombreTaller = prod.nombreOrganizacion || 'el taller';
    const nombreProducto = prod.nombre || 'Este producto';
    const marca = prod.marca || '';
    const condicion = prod.condicion || '';
    const precio = formatearMoneda(prod.precioTotal ? prod.precioTotal : prod.precio);

    const quien = mensajeIdentidad();

    let mensaje = `Hola, ${quien}\n\n`;
    mensaje += `Estoy interesado en el siguiente producto:\n\n`;
    mensaje += `- Producto: ${nombreProducto}\n`;
    if (marca) mensaje += `- Marca: ${marca}\n`;
    if (condicion) mensaje += `- Condición: ${condicion}\n`;
    mensaje += `- Precio: ${precio}\n\n`;
    mensaje += `¿Podríamos coordinar la compra, entrega y pago?`;

    abrirWhatsApp(telefonoTaller, mensaje);
}

function abrirWhatsApp(telefono, mensaje) {
    let tel = String(telefono || '').replace(/\D/g, '');
    if (!tel) {
        alert('El taller no tiene un número de WhatsApp registrado. Comunícate con el vendedor por otro medio.');
        return;
    }
    if (!tel.startsWith('57')) {
        tel = '57' + tel;
    }
    const waUrl = `https://wa.me/${tel}?text=${encodeURIComponent(mensaje)}`;
    window.open(waUrl, '_blank');
}

function cabeceraCsrf() {
    const headers = {};
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) headers[header] = token;
    return headers;
}

async function guardarProducto(prod) {
    const idProducto = prod.idProducto;
    const eraGuardado = guardadosIds().has(String(idProducto));

    try {
        const method = eraGuardado ? 'DELETE' : 'POST';
        const response = await fetch(`/cliente/api/guardados/${idProducto}`, {
            method: method,
            headers: cabeceraCsrf()
        });
        if (!response.ok) throw new Error('No se pudo actualizar tu lista de guardados.');
    } catch (e) {
        console.error(e);
        alert('Hubo un problema al actualizar tu lista de guardados. Intenta de nuevo.');
        return;
    }

    await refrescarGuardados();
    alert(eraGuardado ? 'Producto quitado de tu lista de guardados.' : 'Producto guardado para después.');
}

async function quitarDeGuardados(idProducto) {
    try {
        const response = await fetch(`/cliente/api/guardados/${idProducto}`, {
            method: 'DELETE',
            headers: cabeceraCsrf()
        });
        if (!response.ok) throw new Error('No se pudo quitar el producto.');
    } catch (e) {
        console.error(e);
        alert('Hubo un problema al quitar el producto de tus guardados.');
        return;
    }
    await refrescarGuardados();
    renderizarGuardados();
}

function actualizarContadorGuardados() {
    const contador = document.getElementById("contadorCarrito");
    if (contador) {
        contador.innerText = guardadosServidor.length;
        contador.classList.add("animate__animated", "animate__bounceIn");
        setTimeout(() => contador.classList.remove("animate__animated", "animate__bounceIn"), 1000);
    }
}

function renderizarGuardados() {
    const contenedorItems = document.getElementById("contenedorItemsCarrito");
    if (!contenedorItems) return;

    const guardados = guardadosServidor;

    if (guardados.length === 0) {
        contenedorItems.innerHTML = `
            <div class="text-center py-5 bg-white rounded shadow-sm">
                <i class="bi bi-heart fs-1 text-muted opacity-50 d-block mb-3"></i>
                <h5 class="text-muted">Aún no tienes productos guardados</h5>
                <p class="text-muted small">Guarda los productos que te interesan para verlos después y ponerte en contacto con el vendedor.</p>
                <a href="/cliente" class="btn btn-primary mt-3">Explorar el catálogo</a>
            </div>
        `;
        return;
    }

    const grupos = guardados.reduce((acc, item) => {
        if (!acc[item.idOrganizacion]) {
            acc[item.idOrganizacion] = {
                nombreTaller: item.nombreOrganizacion || 'Taller Independiente',
                telefonoTaller: item.telefonoOrganizacion || '',
                items: []
            };
        }
        acc[item.idOrganizacion].items.push(item);
        return acc;
    }, {});

    let html = `<div class="alert alert-info border-0 d-flex align-items-center mb-4">
            <i class="bi bi-info-circle fs-4 me-3"></i>
            <div><strong>Lista de guardados</strong> — estos son productos que te interesan. No generan ningún pedido: ponte en contacto con el vendedor para coordinar tu compra.</div>
        </div>`;

    for (const idOrg in grupos) {
        const taller = grupos[idOrg];

        html += `
            <div class="card mb-4 shadow-sm border-0 rounded-3">
                <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
                    <h5 class="fw-bold m-0 text-dark"><i class="bi bi-shop me-2 text-primary"></i> ${taller.nombreTaller}</h5>
                    <a class="btn btn-success btn-sm rounded-pill" onclick='contactarDuenoDesdeGuardados("${idOrg}")' href="javascript:void(0)">
                        <i class="bi bi-whatsapp me-1"></i> Contactar vendedor
                    </a>
                </div>
                <div class="card-body p-4">
        `;

        taller.items.forEach(item => {
            const precioFormateado = formatearMoneda(item.precioTotal ? item.precioTotal : item.precio);
            const condicion = item.condicion ? `<p class="text-muted small mb-2">Condición: ${item.condicion}</p>` : '';
            const imagenItem = (item.imagen && item.imagen !== 'null' && item.imagen.trim() !== '') ? `/recursos/${item.imagen}` : 'https://via.placeholder.com/80x80.png?text=Sin+Foto';

            html += `
                <div class="d-flex align-items-center gap-3 mb-3 border-bottom pb-3">
                    <img src="${imagenItem}" alt="${item.nombre}" style="width: 80px; height: 80px; object-fit: contain; background: #f1f5f9; border-radius: 8px; padding: 0.5rem;" onerror="this.src='https://via.placeholder.com/80x80.png?text=Sin+Foto'">
                    <div class="flex-grow-1">
                        <h6 class="fw-bold mb-1">${item.nombre}</h6>
                        <p class="text-muted small mb-2">Marca: ${item.marca}</p>
                        ${condicion}
                        <h6 class="text-primary fw-bold mb-0">${precioFormateado}</h6>
                    </div>
                    <div class="d-flex flex-column align-items-end gap-2">
                        <button class="btn btn-outline-success btn-sm rounded-pill" onclick='contactarDuenoGuardado(${JSON.stringify(item)})'>
                            <i class="bi bi-whatsapp me-1"></i> Contactar
                        </button>
                        <button class="btn btn-outline-danger btn-sm" title="Quitar de guardados" onclick="quitarDeGuardados('${item.idProducto}')"><i class="bi bi-trash"></i></button>
                    </div>
                </div>
            `;
        });

        html += `</div></div>`;
    }

    contenedorItems.innerHTML = html;
}

function contactarDuenoDesdeGuardados(idOrganizacion) {
    const items = guardadosServidor.filter(item => String(item.idOrganizacion) === String(idOrganizacion));
    if (items.length === 0) return;

    const nombreTaller = items[0].nombreOrganizacion || 'Taller Independiente';
    const telefonoTaller = items[0].telefonoOrganizacion || '';
    const quien = mensajeIdentidad();

    let mensaje = `Hola, ${quien}\n\n`;
    mensaje += `Estoy interesado en estos productos de ${nombreTaller}:\n\n`;
    items.forEach(item => {
        mensaje += `- ${item.nombre} (Marca: ${item.marca || 'N/A'}) - ${formatearMoneda(item.precioTotal ? item.precioTotal : item.precio)}\n`;
    });
    mensaje += `\n¿Podríamos coordinar la compra, entrega y pago?`;

    abrirWhatsApp(telefonoTaller, mensaje);
}

function contactarDuenoGuardado(item) {
    const prod = {
        nombre: item.nombre,
        marca: item.marca,
        condicion: item.condicion,
        precio: item.precio,
        precioTotal: item.precioTotal,
        telefonoOrganizacion: item.telefonoOrganizacion,
        nombreOrganizacion: item.nombreOrganizacion
    };
    contactarDueno(prod);
}
