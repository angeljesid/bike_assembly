let productosData = [];

document.addEventListener("DOMContentLoaded", () => {
    cargarProductos();
    actualizarContadorCarrito();

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

    if (window.location.pathname.includes('/cliente/carrito')) {
        renderizarCarrito();
    }
});

function obtenerCartKey() {
    return window.CART_KEY || 'carritoBikeAssembly';
}

function obtenerCarrito() {
    return JSON.parse(localStorage.getItem(obtenerCartKey())) || [];
}

function guardarCarrito(carrito) {
    localStorage.setItem(obtenerCartKey(), JSON.stringify(carrito));
    actualizarContadorCarrito();
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

    productos.forEach(prod => {
        const precioAMostrar = prod.precioTotal ? prod.precioTotal : prod.precio;
        const precioFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(precioAMostrar);
        
        const imagen = (prod.imagen && prod.imagen !== 'null' && prod.imagen.trim() !== '') 
            ? `/recursos/${prod.imagen}` 
            : 'https://via.placeholder.com/80x80.png?text=Sin+Foto'; 
            
        const categoria = prod.categoria ? prod.categoria : 'General'; 
        const marca = prod.marca ? prod.marca : 'Sin marca';
        const condicion = prod.condicion ? prod.condicion : 'N/A';
        const nombreTaller = (prod.organizacion && prod.organizacion.nombre) ? prod.organizacion.nombre : 'Taller Independiente';

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
                            <button class="btn btn-primary btn-sm rounded-circle shadow-sm" style="width: 35px; height: 35px;" onclick='agregarAlCarritoDirecto(${JSON.stringify(prod)})' title="Agregar al carrito">
                                <i class="bi bi-plus-lg"></i>
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
        const nombreMatch = prod.nombre && prod.nombre.toLowerCase().includes(busqueda);
        const marcaMatch = prod.marca && prod.marca.toLowerCase().includes(busqueda);
        const categoriaMatch = prod.categoria && prod.categoria.toLowerCase().includes(busqueda);
        
        return nombreMatch || marcaMatch || categoriaMatch;
    });
    
    renderizarProductos(filtrados);
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
            if (stockEl) stockEl.innerText = (producto.stock || 0) + ' und';
            
            const precioFinal = producto.precioTotal ? producto.precioTotal : producto.precio;
            if (precEl) precEl.innerText = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(precioFinal);
            
            window.productoActualSeleccionado = producto;
        }
    } catch (e) {
        console.error("Error cargando detalle:", e);
    }
}

function añadirDesdeDetalle() {
    if (!window.productoActualSeleccionado) return;
    const cantidadInput = document.getElementById("cantidadProducto");
    const cantidad = cantidadInput ? parseInt(cantidadInput.value) || 1 : 1;
    
    for(let i = 0; i < cantidad; i++) {
        agregarAlCarritoDirecto(window.productoActualSeleccionado);
    }
    alert("¡Producto(s) añadido(s) al carrito con éxito!");
}

function agregarAlCarritoDirecto(prod) {
    let carrito = obtenerCarrito();
    const precioFinal = prod.precioTotal ? prod.precioTotal : prod.precio;
    const imagen = (prod.imagen && prod.imagen !== 'null' && prod.imagen.trim() !== '') 
        ? `/recursos/${prod.imagen}` 
        : 'https://via.placeholder.com/80x80.png?text=Sin+Foto';

    const idOrg = prod.organizacion ? prod.organizacion.idOrganizacion : 0;
    const nombreTaller = (prod.organizacion && prod.organizacion.nombre) ? prod.organizacion.nombre : 'Taller Independiente';
    const telefonoTaller = (prod.organizacion && prod.organizacion.telefono) ? prod.organizacion.telefono : '';

    const productoExistente = carrito.find(item => item.idProducto === prod.idProducto);
    if (productoExistente) {
        productoExistente.cantidad += 1;
    } else {
        carrito.push({
            idProducto: prod.idProducto,
            nombre: prod.nombre,
            marca: prod.marca || 'Sin marca',
            precio: precioFinal,
            imagen: imagen,
            cantidad: 1,
            idOrganizacion: idOrg,
            nombreTaller: nombreTaller,
            telefonoTaller: telefonoTaller
        });
    }
    guardarCarrito(carrito);
}

function actualizarContadorCarrito() {
    const carrito = obtenerCarrito();
    const totalItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);
    const contador = document.getElementById("contadorCarrito");
    if (contador) {
        contador.innerText = totalItems;
        contador.classList.add("animate__animated", "animate__bounceIn");
        setTimeout(() => contador.classList.remove("animate__animated", "animate__bounceIn"), 1000);
    }
}

function renderizarCarrito() {
    const contenedorItems = document.getElementById("contenedorItemsCarrito");
    if (!contenedorItems) return;

    const carrito = obtenerCarrito();

    if (carrito.length === 0) {
        contenedorItems.innerHTML = `
            <div class="text-center py-5 bg-white rounded shadow-sm">
                <i class="bi bi-cart-x fs-1 text-muted opacity-50 d-block mb-3"></i>
                <h5 class="text-muted">Tu carrito está vacío</h5>
                <a href="/cliente" class="btn btn-primary mt-3">Ir a configurar bicicleta</a>
            </div>
        `;
        return;
    }

    const grupos = carrito.reduce((acc, item) => {
        if (!acc[item.idOrganizacion]) {
            acc[item.idOrganizacion] = {
                nombreTaller: item.nombreTaller,
                telefonoTaller: item.telefonoTaller,
                items: [],
                total: 0
            };
        }
        acc[item.idOrganizacion].items.push(item);
        acc[item.idOrganizacion].total += (item.precio * item.cantidad);
        return acc;
    }, {});

    let html = "";

    for (const idOrg in grupos) {
        const taller = grupos[idOrg];
        const totalFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(taller.total);

        html += `
            <div class="card mb-4 shadow-sm border-0 rounded-4">
                <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
                    <h5 class="fw-bold m-0 text-dark"><i class="bi bi-shop me-2 text-primary"></i> ${taller.nombreTaller}</h5>
                </div>
                <div class="card-body p-4">
        `;

        taller.items.forEach(item => {
            const totalItem = item.precio * item.cantidad;
            const precioFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(totalItem);
            const index = carrito.findIndex(i => i.idProducto === item.idProducto);

            html += `
                <div class="d-flex align-items-center gap-3 mb-3 border-bottom pb-3">
                    <img src="${item.imagen}" alt="${item.nombre}" style="width: 80px; height: 80px; object-fit: contain; background: #f1f5f9; border-radius: 8px; padding: 0.5rem;" onerror="this.src='https://via.placeholder.com/80x80.png?text=Sin+Foto'">
                    <div class="flex-grow-1">
                        <h6 class="fw-bold mb-1">${item.nombre}</h6>
                        <p class="text-muted small mb-2">Marca: ${item.marca}</p>
                        <h6 class="text-primary fw-bold mb-0">${precioFormateado}</h6>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <input type="number" class="form-control text-center form-control-sm" value="${item.cantidad}" min="1" style="width: 60px;" onchange="cambiarCantidad(${index}, this.value)">
                        <button class="btn btn-outline-danger btn-sm" title="Eliminar" onclick="eliminarDelCarrito(${index})"><i class="bi bi-trash"></i></button>
                    </div>
                </div>
            `;
        });

        html += `
                </div>
                <div class="card-footer bg-light border-top-0 py-3 d-flex justify-content-between align-items-center rounded-bottom-4">
                    <h5 class="fw-bold m-0 text-dark">Total: ${totalFormateado}</h5>
                    <button class="btn btn-success fw-bold px-4 rounded-pill shadow-sm" onclick="procesarPagoPorTaller('${idOrg}')">
                        <i class="bi bi-whatsapp me-2"></i> Solicitar Reserva
                    </button>
                </div>
            </div>
        `;
    }

    contenedorItems.innerHTML = html;
}

function cambiarCantidad(index, nuevaCantidad) {
    let carrito = obtenerCarrito();
    let cant = parseInt(nuevaCantidad);
    if (cant > 0) {
        carrito[index].cantidad = cant;
        guardarCarrito(carrito);
        renderizarCarrito();
    }
}

function eliminarDelCarrito(index) {
    let carrito = obtenerCarrito();
    carrito.splice(index, 1);
    guardarCarrito(carrito);
    renderizarCarrito();
}

function procesarPagoPorTaller(idOrganizacion) {
    let carrito = obtenerCarrito();
    const itemsTaller = carrito.filter(item => String(item.idOrganizacion) === String(idOrganizacion));

    if (itemsTaller.length === 0) return;

    const detalles = itemsTaller.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioUnitario: item.precio,
        subtotal: item.precio * item.cantidad
    }));

    const total = itemsTaller.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
    const telefonoTaller = itemsTaller[0].telefonoTaller;
    const nombreTaller = itemsTaller[0].nombreTaller;

    const payload = {
        metodoPago: 'Acuerdo con el vendedor',
        total: total,
        detalles: detalles,
        estado: 'PENDIENTE'
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/cliente/api/checkout', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            carrito = carrito.filter(item => String(item.idOrganizacion) !== String(idOrganizacion));
            guardarCarrito(carrito);
            renderizarCarrito();

            let mensaje = `Hola, vengo de Bike Assembly. Quiero hacer este pedido a ${nombreTaller}:\n\n`;
            itemsTaller.forEach(item => {
                const subtotalItem = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(item.precio * item.cantidad);
                mensaje += `- ${item.cantidad}x ${item.nombre} (${subtotalItem})\n`;
            });
            const totalFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(total);
            mensaje += `\nTotal: ${totalFormateado}\n\nQuedo atento para coordinar la entrega y el pago.`;

            let telefonoFormat = telefonoTaller.replace(/\D/g, '');
            if (!telefonoFormat.startsWith('57')) {
                telefonoFormat = '57' + telefonoFormat;
            }

            const waUrl = `https://wa.me/${telefonoFormat}?text=${encodeURIComponent(mensaje)}`;
            window.open(waUrl, '_blank');
        } else {
            alert('Error al registrar la solicitud: ' + (data.mensaje || 'Error desconocido'));
        }
    })
    .catch(err => {
        console.error(err);
        alert('Error de conexión al procesar la solicitud.');
    });
}