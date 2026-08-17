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

        const cardHTML = `
            <div class="col-12 col-sm-6 col-lg-4 col-xl-3">
                <div class="card product-card h-100">
                    <a href="/cliente/producto/${prod.idProducto}" class="text-decoration-none text-dark">
                        <div class="img-container p-3">
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
                            <button class="btn btn-primary btn-sm rounded-circle" style="width: 35px; height: 35px;" onclick='agregarAlCarritoDirecto(${JSON.stringify(prod)})' title="Agregar al carrito">
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
            cantidad: 1
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
    const carrito = obtenerCarrito();
    const contenedorItems = document.getElementById("contenedorItemsCarrito");
    const subtotalEl = document.getElementById("subtotalCarrito");
    const totalEl = document.getElementById("totalCarrito");
    
    if (!contenedorItems) return;

    if (carrito.length === 0) {
        contenedorItems.innerHTML = `
            <div class="text-center py-5 bg-white rounded shadow-sm">
                <i class="bi bi-cart-x fs-1 text-muted opacity-50 d-block mb-3"></i>
                <h5 class="text-muted">Tu carrito está vacío</h5>
                <a href="/cliente" class="btn btn-primary mt-3">Ir a configurar bicicleta</a>
            </div>
        `;
        if (subtotalEl) subtotalEl.innerText = "$ 0";
        if (totalEl) totalEl.innerText = "$ 0";
        return;
    }

    let html = "";
    let subtotalGeneral = 0;

    carrito.forEach((item, index) => {
        const totalItem = item.precio * item.cantidad;
        subtotalGeneral += totalItem;
        const precioFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(totalItem);

        html += `
            <div class="cart-item">
                <img src="${item.imagen}" alt="${item.nombre}" onerror="this.src='https://via.placeholder.com/80x80.png?text=Sin+Foto'">
                <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">${item.nombre}</h5>
                    <p class="text-muted small mb-2">Marca: ${item.marca}</p>
                    <h5 class="text-primary fw-bold mb-0">${precioFormateado}</h5>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <input type="number" class="form-control text-center" value="${item.cantidad}" min="1" style="width: 70px;" onchange="cambiarCantidad(${index}, this.value)">
                    <button class="btn btn-outline-danger btn-sm" title="Eliminar" onclick="eliminarDelCarrito(${index})"><i class="bi bi-trash"></i></button>
                </div>
            </div>
        `;
    });

    contenedorItems.innerHTML = html;
    const subtotalFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(subtotalGeneral);
    if (subtotalEl) subtotalEl.innerText = subtotalFormateado;
    if (totalEl) totalEl.innerText = subtotalFormateado;
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
