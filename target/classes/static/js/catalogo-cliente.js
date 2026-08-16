let productosData = []; 

document.addEventListener("DOMContentLoaded", () => {
    cargarProductos();

    const buscador = document.getElementById("buscadorCliente");
    if(buscador) {
        buscador.addEventListener("keyup", (e) => {
            filtrarProductos(e.target.value);
        });
    }
});

async function cargarProductos() {
    try {
        const response = await fetch('/api/productos');
        if (!response.ok) throw new Error("Error en la respuesta del servidor");
        
        productosData = await response.json();
        
        document.getElementById("mensajeCarga").style.display = "none";
        document.getElementById("contenedorProductos").style.display = "flex";
        
        renderizarProductos(productosData);
    } catch (error) {
        console.error("Error al cargar productos:", error);
        document.getElementById("mensajeCarga").innerHTML = 
            `<p class="text-danger fw-bold">Hubo un problema al cargar el catálogo. Intenta recargar la página.</p>`;
    }
}

function renderizarProductos(productos) {
    const contenedor = document.getElementById("contenedorProductos");
    const msjSinResultados = document.getElementById("mensajeSinResultados");
    
    contenedor.innerHTML = ""; 

    if (productos.length === 0) {
        contenedor.style.display = "none";
        msjSinResultados.style.display = "block";
        return;
    }

    contenedor.style.display = "flex";
    msjSinResultados.style.display = "none";

    productos.forEach(prod => {
        const precioAMostrar = prod.precioTotal ? prod.precioTotal : prod.precio;
        const precioFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(precioAMostrar);
        
        const imagen = (prod.imagen && prod.imagen !== 'null') ? `/recursos/${prod.imagen}` : '/img/placeholder.png'; 
        const categoria = prod.categoria ? prod.categoria : 'General'; 
        const marca = prod.marca ? prod.marca : 'Sin marca';
        const condicion = prod.condicion ? prod.condicion : 'N/A';

        const cardHTML = `
            <div class="col-12 col-sm-6 col-lg-4 col-xl-3">
                <div class="card product-card h-100">
                    <a href="/cliente/producto/${prod.idProducto}" class="text-decoration-none text-dark">
                        <div class="img-container p-3">
                            <img src="${imagen}" alt="${prod.nombre}" onerror="this.src='/img/placeholder.png'">
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
                            <button class="btn btn-primary btn-sm rounded-circle" style="width: 35px; height: 35px;" onclick="agregarAlCarrito(${prod.idProducto})" title="Agregar al carrito">
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

function agregarAlCarrito(idProducto) {
    console.log("Añadiendo producto ID:", idProducto);
    
    const contador = document.getElementById("contadorCarrito");
    let num = parseInt(contador.innerText);
    contador.innerText = num + 1;
    
    contador.classList.add("animate__animated", "animate__bounceIn");
    setTimeout(() => contador.classList.remove("animate__animated", "animate__bounceIn"), 1000);
}