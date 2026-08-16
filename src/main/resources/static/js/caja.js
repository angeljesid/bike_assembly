document.addEventListener('DOMContentLoaded', () => {

    let carrito = [];
    const inputBuscador = document.getElementById('buscadorCaja');
    const tablaCarrito = document.getElementById('tablaCarrito');
    const resumenSubtotal = document.getElementById('resumenSubtotal');
    const resumenTotal = document.getElementById('resumenTotal');
    const btnCobrar = document.getElementById('btnCobrar');
    const metodoPago = document.getElementById('metodoPago');
    const divCambio = document.getElementById('divCambio');
    const montoRecibido = document.getElementById('montoRecibido');
    const montoCambio = document.getElementById('montoCambio');

    // Extraer tokens de seguridad de las metas del HTML
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    inputBuscador.focus();
    document.addEventListener('click', (e) => {
        if (!e.target.closest('button') && !e.target.closest('input') && !e.target.closest('select')) {
            inputBuscador.focus();
        }
    });

    setInterval(() => {
        const ahora = new Date();
        document.getElementById('relojCaja').textContent = ahora.toLocaleTimeString();
    }, 1000);

    inputBuscador.addEventListener('keypress', async (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const codigo = inputBuscador.value.trim();
            if (codigo !== '') {
                await buscarYAgregarProducto(codigo);
            }
        }
    });

    async function buscarYAgregarProducto(codigo) {
        try {
            const response = await fetch(`/admin/caja/api/buscar-producto?codigo=${codigo}`);
            
            if (!response.ok) {
                alert('Producto no encontrado.');
                inputBuscador.value = '';
                return;
            }

            const producto = await response.json();
            
            if (producto.stock <= 0) {
                alert(`El producto ${producto.nombre} está agotado.`);
                inputBuscador.value = '';
                return;
            }

            agregarAlCarrito(producto);
            inputBuscador.value = ''; 
            
        } catch (error) {
            console.error("Error buscando producto:", error);
        }
    }

    function agregarAlCarrito(producto) {
        const itemExistente = carrito.find(item => item.idProducto === producto.idProducto);

        // Se asume que en tu entidad el precio está en producto.precioTotal
        const precioP = producto.precioTotal || producto.precio; 

        if (itemExistente) {
            if (itemExistente.cantidad < producto.stock) {
                itemExistente.cantidad++;
                itemExistente.subtotal = itemExistente.cantidad * itemExistente.precioUnitario;
            } else {
                alert('No hay suficiente stock.');
            }
        } else {
            carrito.push({
                idProducto: producto.idProducto,
                nombre: producto.nombre,
                precioUnitario: precioP, 
                cantidad: 1,
                subtotal: precioP,
                stockDisponible: producto.stock
            });
        }
        actualizarUI();
    }

    function actualizarUI() {
        tablaCarrito.innerHTML = '';
        let total = 0;

        if (carrito.length === 0) {
            tablaCarrito.innerHTML = `
                <tr id="fila-vacia">
                    <td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-cart-x fs-1 d-block mb-2"></i>
                        El carrito está vacío.
                    </td>
                </tr>`;
            btnCobrar.disabled = true;
        } else {
            btnCobrar.disabled = false;
            
            carrito.forEach((item, index) => {
                total += item.subtotal;
                const fila = `
                    <tr>
                        <td class="ps-4 fw-bold text-dark">${item.nombre}</td>
                        <td class="text-end text-muted">$ ${item.precioUnitario.toFixed(2)}</td>
                        <td class="text-center">
                            <div class="input-group input-group-sm mx-auto" style="width: 100px;">
                                <button class="btn btn-outline-secondary btn-restar" data-index="${index}">-</button>
                                <input type="text" class="form-control text-center" value="${item.cantidad}" readonly>
                                <button class="btn btn-outline-secondary btn-sumar" data-index="${index}">+</button>
                            </div>
                        </td>
                        <td class="text-end fw-bold" style="color: var(--brand-blue);">$ ${item.subtotal.toFixed(2)}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-danger btn-eliminar" data-index="${index}">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
                tablaCarrito.insertAdjacentHTML('beforeend', fila);
            });
        }

        resumenSubtotal.textContent = `$ ${total.toFixed(2)}`;
        resumenTotal.textContent = `$ ${total.toFixed(2)}`;
        calcularCambio(total);

        document.querySelectorAll('.btn-sumar').forEach(btn => {
            btn.addEventListener('click', (e) => modificarCantidad(e.target.closest('button').dataset.index, 1));
        });
        document.querySelectorAll('.btn-restar').forEach(btn => {
            btn.addEventListener('click', (e) => modificarCantidad(e.target.closest('button').dataset.index, -1));
        });
        document.querySelectorAll('.btn-eliminar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const index = e.target.closest('button').dataset.index;
                carrito.splice(index, 1);
                actualizarUI();
            });
        });
    }

    function modificarCantidad(index, cambio) {
        const item = carrito[index];
        const nuevaCantidad = item.cantidad + cambio;

        if (nuevaCantidad > 0 && nuevaCantidad <= item.stockDisponible) {
            item.cantidad = nuevaCantidad;
            item.subtotal = item.cantidad * item.precioUnitario;
        } else if (nuevaCantidad === 0) {
            carrito.splice(index, 1);
        } else {
            alert('Límite de stock alcanzado.');
        }
        actualizarUI();
    }

    metodoPago.addEventListener('change', () => {
        if (metodoPago.value === 'EFECTIVO') {
            divCambio.style.display = 'block';
        } else {
            divCambio.style.display = 'none';
            montoRecibido.value = '';
            montoCambio.textContent = '$ 0.00';
        }
    });

    montoRecibido.addEventListener('input', () => {
        const total = carrito.reduce((acc, item) => acc + item.subtotal, 0);
        calcularCambio(total);
    });

    function calcularCambio(totalVenta) {
        const recibido = parseFloat(montoRecibido.value) || 0;
        if (metodoPago.value === 'EFECTIVO' && recibido > 0) {
            const cambio = recibido - totalVenta;
            montoCambio.textContent = `$ ${Math.max(0, cambio).toFixed(2)}`;
            if (cambio < 0) {
                montoCambio.classList.add('text-danger');
                montoCambio.classList.remove('text-success');
            } else {
                montoCambio.classList.remove('text-danger');
                montoCambio.classList.add('text-success');
            }
        }
    }

    btnCobrar.addEventListener('click', async () => {
        const totalVenta = carrito.reduce((acc, item) => acc + item.subtotal, 0);
        
        if (metodoPago.value === 'EFECTIVO') {
            const recibido = parseFloat(montoRecibido.value) || 0;
            if (recibido < totalVenta) {
                alert('El monto recibido es insuficiente.');
                montoRecibido.focus();
                return;
            }
        }

        const peticion = {
            metodoPago: metodoPago.value,
            total: totalVenta,
            detalles: carrito.map(item => ({
                idProducto: item.idProducto,
                cantidad: item.cantidad,
                precioUnitario: item.precioUnitario,
                subtotal: item.subtotal
            }))
        };

        // Construir cabeceras incluyendo el CSRF Token
        const fetchHeaders = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            fetchHeaders[csrfHeader] = csrfToken;
        }

        try {
            const response = await fetch('/admin/caja/api/cobrar', {
                method: 'POST',
                headers: fetchHeaders,
                body: JSON.stringify(peticion)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                alert('¡Venta registrada con éxito!');
                carrito = [];
                montoRecibido.value = '';
                actualizarUI();
                inputBuscador.focus();
            } else {
                alert('Error al registrar la venta: ' + (data.mensaje || 'Revisa la consola.'));
            }
        } catch (error) {
            console.error("Error en la transacción:", error);
            alert('Error al conectar con el servidor.');
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'F12') {
            e.preventDefault();
            if (!btnCobrar.disabled) {
                btnCobrar.click();
            }
        }
    });
});