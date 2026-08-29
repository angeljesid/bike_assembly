document.addEventListener('DOMContentLoaded', () => {

    const MAX_PESTANAS = 5;

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
    const formNuevoCliente = document.getElementById('formNuevoCliente');

    const contenedorPestanas = document.getElementById('contenedorPestanas');

    let listaProductosGlobal = [];

    let tabs = [];
    let tabActivaId = null;
    let contadorId = 0;

    const tabActual = () => tabs.find(t => t.id === tabActivaId);

    const fmt = (valor) => {
        if (window.moneda && window.moneda.formato) {
            return window.moneda.formato(valor);
        }
        return `$ ${Number(valor || 0).toFixed(2)}`;
    };

    function crearTab() {
        let numLibre = 1;
        while (tabs.some(t => t.numero === numLibre)) {
            numLibre++;
        }
        contadorId++;
        const tab = {
            id: contadorId,
            numero: numLibre,
            nombre: 'Pestaña ' + numLibre,
            items: [],
            clienteId: null,
            clienteNombre: null,
            clienteCedula: null,
            metodoPago: 'EFECTIVO',
            montoRecibido: ''
        };
        tabs.push(tab);
        tabActivaId = tab.id;
        renderizarPestanas();
        sincronizarControles();
    }

    function cerrarTab(id) {
        const tab = tabs.find(t => t.id === id);
        if (!tab) return;
        if (tab.items.length > 0) {
            if (!confirm('La pestaña "' + tab.nombre + '" tiene ' + tab.items.length + ' artículo(s) en el carrito. ¿Desea eliminar esta venta pendiente?')) {
                return;
            }
        }
        const idx = tabs.indexOf(tab);
        tabs = tabs.filter(t => t.id !== id);

        if (tabActivaId === id) {
            if (tabs.length === 0) {
                crearTab();
            } else {
                const siguiente = tabs[Math.min(idx, tabs.length - 1)];
                tabActivaId = siguiente.id;
            }
        }
        renderizarPestanas();
        sincronizarControles();
    }

    function renderizarPestanas() {
        let html = '';
        tabs.forEach(tab => {
            html += `
                <div class="pestana-item ${tab.id === tabActivaId ? 'pestana-activa' : ''}" data-id="${tab.id}">
                    <i class="bi bi-cart pestana-ico"></i>
                    <span class="pestana-nombre">${tab.nombre}</span>
                    ${tab.items.length > 0 ? '<i class="bi bi-circle-fill pestana-punto text-warning"></i>' : ''}
                    <button type="button" class="pestana-cerrar" data-cerrar="${tab.id}" title="Cerrar pestaña">
                        <i class="bi bi-x-lg"></i>
                    </button>
                </div>
            `;
        });
        if (tabs.length < MAX_PESTANAS) {
            html += '<button type="button" class="pestana-nueva" id="pestanaNueva" title="Nueva pestaña"><i class="bi bi-plus-lg"></i></button>';
        }
        contenedorPestanas.innerHTML = html;

        contenedorPestanas.querySelectorAll('.pestana-item').forEach(el => {
            el.addEventListener('click', (e) => {
                if (e.target.closest('.pestana-cerrar')) return;
                tabActivaId = parseInt(el.getAttribute('data-id'));
                renderizarPestanas();
                sincronizarControles();
            });
        });
        contenedorPestanas.querySelectorAll('.pestana-cerrar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                cerrarTab(parseInt(btn.getAttribute('data-cerrar')));
            });
        });
        const botonNueva = document.getElementById('pestanaNueva');
        if (botonNueva) {
            botonNueva.addEventListener('click', () => {
                if (tabs.length < MAX_PESTANAS) {
                    crearTab();
                }
            });
        }
    }

    function sincronizarControles() {
        const tab = tabActual();
        if (!tab) return;

        renderizarCarrito();

        if (tab.clienteId) {
            inputIdClienteSeleccionado.value = tab.clienteId;
            clienteSeleccionadoInfo.innerHTML = `Cliente: ${tab.clienteNombre} <br><small>CC: ${tab.clienteCedula}</small>`;
            clienteSeleccionadoInfo.style.display = 'block';
        } else {
            inputIdClienteSeleccionado.value = '';
            clienteSeleccionadoInfo.style.display = 'none';
            clienteSeleccionadoInfo.textContent = '';
        }
        inputBuscadorCliente.value = '';
        metodoPagoSelect.value = tab.metodoPago;
        montoRecibidoInput.value = tab.montoRecibido;
        calcularCambio();

    }

    async function cargarProductosCaja() {
        try {
            const response = await fetch('/caja/api/productos');
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
            const res = await fetch(`/caja/api/buscar-cliente?termino=${encodeURIComponent(termino)}`);
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
                            seleccionarCliente(btn.getAttribute('data-id'), btn.getAttribute('data-nombre'), btn.getAttribute('data-cedula'));
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

    function seleccionarCliente(id, nombre, cedula) {
        const tab = tabActual();
        if (!tab) return;
        tab.clienteId = id;
        tab.clienteNombre = nombre;
        tab.clienteCedula = cedula;
        inputIdClienteSeleccionado.value = id;
        clienteSeleccionadoInfo.innerHTML = `Cliente: ${nombre} <br><small>CC: ${cedula}</small>`;
        clienteSeleccionadoInfo.style.display = 'block';
        inputBuscadorCliente.value = '';
        listaSugerenciasCliente.style.display = 'none';
        renderizarPestanas();
        metodoPagoSelect.focus();
    }

    if(formNuevoCliente) {
        formNuevoCliente.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                nombre: document.getElementById('nuevoNombre').value.trim(),
                cedula: document.getElementById('nuevaCedula').value.trim(),
                email: document.getElementById('nuevoCorreo').value.trim() || null
            };

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

            try {
                const res = await fetch('/caja/api/cliente/nuevo', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(payload)
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    seleccionarCliente(data.idUsuario, data.nombre, data.cedula);
                    const modal = bootstrap.Modal.getInstance(document.getElementById('modalNuevoCliente'));
                    modal.hide();
                    formNuevoCliente.reset();
                } else {
                    alert('Error: ' + (data.mensaje || 'No se pudo crear el cliente'));
                }
            } catch (err) {
                console.error(err);
                alert('Error de conexión al crear cliente.');
            }
        });
    }

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
                        <span class="badge bg-primary rounded-pill fs-6">${fmt(precio)}</span>
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
                    inputBuscador.focus();
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
                fetch(`/caja/api/buscar-producto?codigo=${encodeURIComponent(codigo)}`)
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
        const tab = tabActual();
        if (!tab) return;
        const stockDisponible = producto.stock != null ? producto.stock : 0;
        if (stockDisponible <= 0) {
            alert('Producto sin stock disponible');
            return;
        }

        const existente = tab.items.find(item => item.idProducto === producto.idProducto);
        const precioUn = producto.precioTotal != null ? producto.precioTotal : (producto.precio != null ? producto.precio : 0);

        if (existente) {
            if (existente.cantidad < stockDisponible) {
                existente.cantidad++;
                existente.subtotal = existente.cantidad * existente.precioUnitario;
            } else {
                alert('No hay más stock disponible');
            }
        } else {
            tab.items.push({
                idProducto: producto.idProducto,
                nombre: producto.nombre,
                precioUnitario: precioUn,
                cantidad: 1,
                subtotal: precioUn,
                stockMax: stockDisponible
            });
        }
        renderizarPestanas();
        renderizarCarrito();
    }

    window.cambiarCantidad = function(idProducto, delta) {
        const tab = tabActual();
        if (!tab) return;
        const item = tab.items.find(i => i.idProducto === idProducto);
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
        const tab = tabActual();
        if (!tab) return;
        tab.items = tab.items.filter(i => i.idProducto !== idProducto);
        renderizarPestanas();
        renderizarCarrito();
    };

    function renderizarCarrito() {
        const tab = tabActual();
        if (!tab) return;

        if (tab.items.length === 0) {
            tablaCarrito.innerHTML = `
                <tr id="fila-vacia">
                    <td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-cart-x fs-1 d-block mb-2"></i>
                        El carrito está vacío. Escanea un producto para comenzar.
                    </td>
                </tr>
            `;
            resumenSubtotal.textContent = fmt(0);
            resumenTotal.textContent = fmt(0);
            btnCobrar.disabled = true;
            return;
        }

        let html = '';
        let totalGeneral = 0;

        tab.items.forEach(item => {
            totalGeneral += item.subtotal;
            html += `
                <tr>
                    <td class="ps-4 fw-bold text-dark">${item.nombre}</td>
                    <td class="text-end text-muted">${fmt(item.precioUnitario)}</td>
                    <td class="text-center">
                        <div class="input-group input-group-sm justify-content-center">
                            <button class="btn btn-outline-secondary px-2" onclick="cambiarCantidad(${item.idProducto}, -1)">-</button>
                            <span class="px-3 border d-flex align-items-center bg-white fw-bold">${item.cantidad}</span>
                            <button class="btn btn-outline-secondary px-2" onclick="cambiarCantidad(${item.idProducto}, 1)">+</button>
                        </div>
                    </td>
                    <td class="text-end fw-bold">${fmt(item.subtotal)}</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-danger border-0" onclick="eliminarItem(${item.idProducto})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        tablaCarrito.innerHTML = html;
        resumenSubtotal.textContent = fmt(totalGeneral);
        resumenTotal.textContent = fmt(totalGeneral);
        btnCobrar.disabled = false;
        calcularCambio();
    }

    montoRecibidoInput.addEventListener('input', () => {
        const tab = tabActual();
        if (!tab) return;
        tab.montoRecibido = montoRecibidoInput.value;
        calcularCambio();
    });

    metodoPagoSelect.addEventListener('change', () => {
        const tab = tabActual();
        if (!tab) return;
        tab.metodoPago = metodoPagoSelect.value;
    });

    function calcularCambio() {
        const tab = tabActual();
        if (!tab) return;
        const totalGeneral = tab.items.reduce((acc, item) => acc + item.subtotal, 0);
        const recibido = parseFloat(montoRecibidoInput.value) || 0;
        const cambio = recibido - totalGeneral;
        montoCambioSpan.textContent = fmt(cambio >= 0 ? cambio : 0);
    }

    btnCobrar.addEventListener('click', async () => {
        const tab = tabActual();
        if (!tab || tab.items.length === 0) return;

        const totalGeneral = tab.items.reduce((acc, item) => acc + item.subtotal, 0);
        const metodoPago = tab.metodoPago;
        const recibido = parseFloat(tab.montoRecibido) || 0;

        if (metodoPago === 'EFECTIVO' && recibido < totalGeneral) {
            alert('El monto recibido es menor al total de la venta.');
            return;
        }

        const detalles = tab.items.map(item => ({
            idProducto: item.idProducto,
            cantidad: item.cantidad,
            precioUnitario: item.precioUnitario,
            subtotal: item.subtotal
        }));

        const ventaRequest = {
            metodoPago: metodoPago,
            total: totalGeneral,
            idCliente: tab.clienteId ? parseInt(tab.clienteId) : null,
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
            const response = await fetch('/caja/api/cobrar', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(ventaRequest)
            });

            const resultado = await response.json();
            if (response.ok && resultado.success) {
                window.open(`/caja/factura/${resultado.idVenta}`, '_blank');
                tab.items = [];
                tab.clienteId = null;
                tab.clienteNombre = null;
                tab.clienteCedula = null;
                tab.metodoPago = 'EFECTIVO';
                tab.montoRecibido = '';
                renderizarPestanas();
                sincronizarControles();
                inputBuscador.focus();
                cargarProductosCaja();
            } else {
                alert('Error al cobrar: ' + (resultado.mensaje || 'Error desconocido'));
            }
        } catch (error) {
            console.error('Error en cobro:', error);
            alert('Error de red al procesar el cobro');
        }
    });

    crearTab();

});
