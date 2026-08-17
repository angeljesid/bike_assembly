document.addEventListener('DOMContentLoaded', () => {

    const normalizar = (texto) => {
        if (!texto) return '';
        return texto.toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();
    };

    const inputBuscador = document.getElementById('buscadorCaja');
    const listaSugerencias = document.getElementById('listaSugerenciasCaja');
    const tablaCarrito = document.getElementById('tablaCarrito');
    const resumenSubtotal = document.getElementById('resumenSubtotal');
    const resumenTotal = document.getElementById('resumenTotal');
    const btnCobrar = document.getElementById('btnCobrar');
    const metodoPagoSelect = document.getElementById('metodoPago');
    const montoRecibidoInput = document.getElementById('montoRecibido');
    const montoCambioSpan = document.getElementById('montoCambio');
    const relojCaja = document.getElementById('relojCaja');

    const inputBuscadorCliente = document.getElementById('buscadorCliente');
    const listaSugerenciasCliente = document.getElementById('listaSugerenciasCliente');
    const clienteSeleccionadoInfo = document.getElementById('clienteSeleccionadoInfo');
    const inputIdClienteSeleccionado = document.getElementById('idClienteSeleccionado');

    let listaProductosGlobal = [];
    let carrito = [];

    async function cargarProductosCaja() {
        try {
            const response = await fetch('/admin/caja/api/productos');
            if (!response.ok) throw new Error('Error al cargar productos');
            listaProductosGlobal = await response.json();
        } catch (error) {
            console.error('Error:', error);
        }
    }

    cargarProductosCaja();

    if (relojCaja) {
        setInterval(() => {
            const ahora = new Date();
            relojCaja.textContent = ahora.toLocaleTimeString();
        }, 1000);
    }

    inputBuscadorCliente.addEventListener('input', async (e) => {
        const termino = e.target.value.trim();
        if (termino.length < 2) {
            listaSugerenciasCliente.style.display = 'none';
            listaSugerenciasCliente.innerHTML = '';
            return;
        }
        try {
            const res = await fetch(`/admin/caja/api/buscar-cliente?termino=${encodeURIComponent(termino)}`);
            if (res.ok) {
                const clientes = await res.json();
                if (clientes.length > 0) {
                    let html = '';
                    clientes.forEach(c => {
                        html += `
                            <button type="button" class="list-group-item list-group-item-action py-2 px-3 border-0 border-bottom text-start" data-id="${c.idUsuario}" data-nombre="${c.nombre}" data-cedula="${c.cedula || 'N/A'}">
                                <span class="fw-bold text-dark">${c.nombre}</span><br>
                                <small class="text-muted">CC: ${c.cedula || 'N/A'}</small>
                            </button>
                        `;
                    });
                    listaSugerenciasCliente.innerHTML = html;
                    listaSugerenciasCliente.style.display = 'block';

                    listaSugerenciasCliente.querySelectorAll('button').forEach(btn => {
                        btn.addEventListener('click', () => {
                            inputIdClienteSeleccionado.value = btn.getAttribute('data-id');
                            clienteSeleccionadoInfo.innerHTML = `Cliente: ${btn.getAttribute('data-nombre')} <br><small>CC: ${btn.getAttribute('data-cedula')}</small>`;
                            clienteSeleccionadoInfo.style.display = 'block';
                            inputBuscadorCliente.value = '';
                            listaSugerenciasCliente.style.display = 'none';
                        });
                    });
                } else {
                    listaSugerenciasCliente.style.display = 'none';
                }
            }
        } catch (err) {
            console.error(err);
        }
    });

    inputBuscador.addEventListener('input', (e) => {
        const termino = e.target.value;
        if (termino.trim().length === 0) {
            listaSugerencias.style.display = 'none';
            listaSugerencias.innerHTML = '';
            return;
        }

        const terminoNorm = normalizar(termino);
        const palabras = terminoNorm.split(/\s+/).filter(p => p.length > 0);

        const filtrados = listaProductosGlobal.filter(p => {
            const textoBusq = normalizar(`${p.nombre || ''} ${p.marca || ''} ${p.codigoBarras || ''} ${p.condicion || ''}`);
            return palabras.every(palabra => textoBusq.includes(palabra));
        });

        if (filtrados.length > 0) {
            let html = '';
            filtrados.forEach(p => {
                const precio = p.precioTotal != null ? p.precioTotal : (p.precio != null ? p.precio : 0);
                const codigo = p.codigoBarras ? p.codigoBarras : `ID-${p.idProducto}`;
                html += `
                    <button type="button" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-2 px-3 border-0 border-bottom" data-id="${p.idProducto}">
                        <div>
                            <span class="fw-bold text-dark">${p.nombre}</span>
                            <small class="text-muted d-block">Marca: ${p.marca || 'N/A'} | Stock: ${p.stock || 0} | Código: ${codigo}</small>
                        </div>
                        <span class="badge bg-primary rounded-pill fs-6">$ ${precio}</span>
                    </button>
                `;
            });
            listaSugerencias.innerHTML = html;
            listaSugerencias.style.display = 'block';

            document.querySelectorAll('#listaSugerenciasCaja button').forEach(btn => {
                btn.addEventListener('click', () => {
                    const idProd = parseInt(btn.getAttribute('data-id'));
                    const productoEncontrado = listaProductosGlobal.find(item => item.idProducto === idProd);
                    if (productoEncontrado) {
                        agregarAlCarrito(productoEncontrado);
                    }
                    inputBuscador.value = '';
                    listaSugerencias.style.display = 'none';
                });
            });
        } else {
            listaSugerencias.style.display = 'none';
            listaSugerencias.innerHTML = '';
        }
    });

    inputBuscador.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const codigo = inputBuscador.value.trim();
            if (!codigo) return;

            const productoEncontrado = listaProductosGlobal.find(p => 
                (p.codigoBarras && p.codigoBarras.toLowerCase() === codigo.toLowerCase()) || 
                p.idProducto.toString() === codigo ||
                normalizar(p.nombre).includes(normalizar(codigo))
            );

            if (productoEncontrado) {
                agregarAlCarrito(productoEncontrado);
                inputBuscador.value = '';
                listaSugerencias.style.display = 'none';
            } else {
                fetch(`/admin/caja/api/buscar-producto?codigo=${encodeURIComponent(codigo)}`)
                    .then(res => {
                        if (!res.ok) throw new Error('No encontrado');
                        return res.json();
                    })
                    .then(p => {
                        agregarAlCarrito(p);
                        inputBuscador.value = '';
                        listaSugerencias.style.display = 'none';
                    })
                    .catch(() => {
                        alert('Producto no encontrado');
                    });
            }
        }
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('#buscadorCaja') && !e.target.closest('#listaSugerenciasCaja')) {
            listaSugerencias.style.display = 'none';
        }
        if (!e.target.closest('#buscadorCliente') && !e.target.closest('#listaSugerenciasCliente')) {
            listaSugerenciasCliente.style.display = 'none';
        }
    });

    function agregarAlCarrito(producto) {
        const stockDisponible = producto.stock != null ? producto.stock : 0;
        if (stockDisponible <= 0) {
            alert('Producto sin stock disponible');
            return;
        }

        const existente = carrito.find(item => item.idProducto === producto.idProducto);
        const precioUn = producto.precioTotal != null ? producto.precioTotal : (producto.precio != null ? producto.precio : 0);

        if (existente) {
            if (existente.cantidad < stockDisponible) {
                existente.cantidad++;
                existente.subtotal = existente.cantidad * existente.precioUnitario;
            } else {
                alert('No hay más stock disponible');
            }
        } else {
            carrito.push({
                idProducto: producto.idProducto,
                nombre: producto.nombre,
                precioUnitario: precioUn,
                cantidad: 1,
                subtotal: precioUn,
                stockMax: stockDisponible
            });
        }
        renderizarCarrito();
    }

    window.cambiarCantidad = function(idProducto, delta) {
        const item = carrito.find(i => i.idProducto === idProducto);
        if (!item) return;

        const nuevaCantidad = item.cantidad + delta;
        if (nuevaCantidad > 0 && nuevaCantidad <= item.stockMax) {
            item.cantidad = nuevaCantidad;
            item.subtotal = item.cantidad * item.precioUnitario;
        } else if (nuevaCantidad > item.stockMax) {
            alert('Stock máximo alcanzado');
        }
        renderizarCarrito();
    };

    window.eliminarItem = function(idProducto) {
        carrito = carrito.filter(i => i.idProducto !== idProducto);
        renderizarCarrito();
    };

    function renderizarCarrito() {
        if (carrito.length === 0) {
            tablaCarrito.innerHTML = `
                <tr id="fila-vacia">
                    <td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-cart-x fs-1 d-block mb-2"></i>
                        El carrito está vacío. Escanea un producto para comenzar.
                    </td>
                </tr>
            `;
            resumenSubtotal.textContent = '$ 0.00';
            resumenTotal.textContent = '$ 0.00';
            btnCobrar.disabled = true;
            return;
        }

        let html = '';
        let totalGeneral = 0;

        carrito.forEach(item => {
            totalGeneral += item.subtotal;
            html += `
                <tr>
                    <td class="ps-4 fw-bold text-dark">${item.nombre}</td>
                    <td class="text-end text-muted">$ ${item.precioUnitario.toFixed(2)}</td>
                    <td class="text-center">
                        <div class="input-group input-group-sm justify-content-center">
                            <button class="btn btn-outline-secondary px-2" onclick="cambiarCantidad(${item.idProducto}, -1)">-</button>
                            <span class="px-3 border d-flex align-items-center bg-white fw-bold">${item.cantidad}</span>
                            <button class="btn btn-outline-secondary px-2" onclick="cambiarCantidad(${item.idProducto}, 1)">+</button>
                        </div>
                    </td>
                    <td class="text-end fw-bold">$ ${item.subtotal.toFixed(2)}</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-danger border-0" onclick="eliminarItem(${item.idProducto})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        tablaCarrito.innerHTML = html;
        resumenSubtotal.textContent = `$ ${totalGeneral.toFixed(2)}`;
        resumenTotal.textContent = `$ ${totalGeneral.toFixed(2)}`;
        btnCobrar.disabled = false;
        calcularCambio();
    }

    montoRecibidoInput.addEventListener('input', calcularCambio);

    function calcularCambio() {
        const totalGeneral = carrito.reduce((acc, item) => acc + item.subtotal, 0);
        const recibido = parseFloat(montoRecibidoInput.value) || 0;
        const cambio = recibido - totalGeneral;
        montoCambioSpan.textContent = `$ ${cambio >= 0 ? cambio.toFixed(2) : '0.00'}`;
    }

    btnCobrar.addEventListener('click', async () => {
        const totalGeneral = carrito.reduce((acc, item) => acc + item.subtotal, 0);
        const metodoPago = metodoPagoSelect.value;
        const recibido = parseFloat(montoRecibidoInput.value) || 0;

        if (metodoPago === 'EFECTIVO' && recibido < totalGeneral) {
            alert('El monto recibido es menor al total de la venta.');
            return;
        }

        const detalles = carrito.map(item => ({
            idProducto: item.idProducto,
            cantidad: item.cantidad,
            precioUnitario: item.precioUnitario,
            subtotal: item.subtotal
        }));

        const idClienteV = inputIdClienteSeleccionado.value ? parseInt(inputIdClienteSeleccionado.value) : null;

        const ventaRequest = {
            metodoPago: metodoPago,
            total: totalGeneral,
            idCliente: idClienteV,
            detalles: detalles
        };

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        try {
            const response = await fetch('/admin/caja/api/cobrar', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(ventaRequest)
            });

            const resultado = await response.json();
            if (response.ok && resultado.success) {
                
                window.open(`/admin/caja/factura/${resultado.idVenta}`, '_blank');
                
                carrito = [];
                renderizarCarrito();
                montoRecibidoInput.value = '';
                inputIdClienteSeleccionado.value = '';
                clienteSeleccionadoInfo.style.display = 'none';
                clienteSeleccionadoInfo.textContent = '';
                cargarProductosCaja();
            } else {
                alert('Error al cobrar: ' + (resultado.mensaje || 'Error desconocido'));
            }
        } catch (error) {
            console.error('Error en cobro:', error);
            alert('Error de red al procesar el cobro');
        }
    });

});