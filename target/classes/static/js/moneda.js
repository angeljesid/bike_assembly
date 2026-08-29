(function (window) {
    "use strict";

    function formatoMoneda(valor) {
        if (valor === null || valor === undefined || isNaN(Number(valor))) {
            return '$ 0,00';
        }
        return new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            minimumFractionDigits: 2
        }).format(Number(valor));
    }

    function parseMoneda(texto) {
        if (!texto) {
            return 0;
        }
        var limpio = String(texto)
            .replace(/[$ ]/g, '')
            .replace(/\./g, '')
            .replace(/,/g, '.');
        var numero = parseFloat(limpio);
        return isNaN(numero) ? 0 : numero;
    }

    function formatearInput(input) {
        if (!input) {
            return;
        }
        input.addEventListener('input', function () {
            var numero = parseMoneda(input.value);
            if (!isNaN(numero)) {
                input.value = numeroConComa(numero);
            }
        });
        input.addEventListener('blur', function () {
            if (input.value) {
                input.value = numeroConComa(parseMoneda(input.value));
            }
        });
    }

    /* Convierte un número a un string con separador de miles y coma decimal,
       sin símbolo de moneda. Ej: 45000 -> "45.000,00" */
    function numeroConComa(numero) {
        var sinSimbolo = Number(numero).toFixed(2).replace('.', ',');
        var partes = sinSimbolo.split(',');
        partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        return partes.join(',');
    }

    var moneda = {
        formato: formatoMoneda,
        parse: parseMoneda,
        formatearInput: formatearInput,
        numeroConComa: numeroConComa,
        formatearCeldas: function (selector) {
            var elementos = document.querySelectorAll(selector);
            elementos.forEach(function (el) {
                var texto = el.getAttribute('data-monto');
                if (texto === null || texto === undefined) {
                    texto = el.textContent;
                }
                var numero = Number(String(texto).replace(/[$ ]/g, ''));
                if (!isNaN(numero)) {
                    el.textContent = formatoMoneda(numero);
                }
            });
        }
    };

    window.moneda = moneda;

    /* =====================================================================
     * Módulo: Registrar Servicio de Bicicleta (vista Caja/Admin)
     * Toda la lógica de la vista opera desde aquí; el HTML solo declara
     * elementos con sus ids/names. Se auto-detecta la página.
     * ===================================================================== */
    function initRegistroServicio() {
        var precioServicioInput = document.getElementById('precioServicioInput');
        if (!precioServicioInput) {
            return;
        }

        var tipoRegistrado = document.getElementById('tipoRegistrado');
        var tipoFinal = document.getElementById('tipoFinal');
        var busquedaSection = document.getElementById('busquedaClienteSection');
        var campoNombre = document.getElementById('campoNombre');
        var campoCedula = document.getElementById('campoCedula');
        var busquedaInput = document.getElementById('busquedaCliente');
        var sugerencias = document.getElementById('sugerencias');
        var clienteSeleccionado = document.getElementById('clienteSeleccionado');
        var clienteIdHidden = document.getElementById('clienteId');
        var inputNombre = document.querySelector('[name="clienteNombre"]');
        var inputCedula = document.querySelector('[name="clienteCedula"]');
        var clienteSelNombre = document.getElementById('clienteSelNombre');
        var clienteSelCedula = document.getElementById('clienteSelCedula');
        var btnLimpiarCliente = document.getElementById('limpiarCliente');

        var precioServicioVal = document.getElementById('precioServicioVal');
        var buscadorRepuesto = document.getElementById('buscadorRepuesto');
        var sugerenciasRepuesto = document.getElementById('sugerenciasRepuesto');
        var tablaRepuestos = document.getElementById('tablaRepuestos');
        var totalServicioEl = document.getElementById('totalServicio');
        var btnAgregarRepuesto = document.getElementById('btnAgregarRepuesto');

        var productosDisponibles = [];
        var repuestosSel = [];

        function setModo(tipo) {
            var registrado = tipo === 'registrado';
            busquedaSection.classList.toggle('d-none', !registrado);
            /* En modo "cliente final" se captura nombre y cédula manualmente;
               en modo "registrado" la búsqueda autocompleta ambos campos. */
            campoCedula.classList.toggle('d-none', registrado);
            campoNombre.classList.remove('d-none');
            if (!registrado) {
                limpiarCliente();
            }
            inputNombre.required = true;
        }

        function limpiarCliente() {
            clienteIdHidden.value = '';
            busquedaInput.value = '';
            clienteSeleccionado.classList.add('d-none');
            inputNombre.readOnly = false;
            inputCedula.readOnly = false;
        }

        if (tipoRegistrado) tipoRegistrado.addEventListener('change', function () { setModo('registrado'); });
        if (tipoFinal) tipoFinal.addEventListener('change', function () { setModo('final'); });
        if (btnLimpiarCliente) btnLimpiarCliente.addEventListener('click', limpiarCliente);

        busquedaInput.addEventListener('input', function () {
            var termino = this.value.trim();
            if (termino.length < 2) {
                sugerencias.classList.add('d-none');
                sugerencias.innerHTML = '';
                if (!clienteSeleccionado.classList.contains('d-none') && clienteIdHidden.value) {
                    limpiarCliente();
                }
                return;
            }
            fetch('/caja/api/buscar-cliente?termino=' + encodeURIComponent(termino))
                .then(function (r) { return r.json(); })
                .then(function (clientes) {
                    sugerencias.innerHTML = '';
                    if (!clientes || clientes.length === 0) {
                        sugerencias.innerHTML = '<button type="button" class="list-group-item list-group-item-action disabled">Sin resultados</button>';
                        sugerencias.classList.remove('d-none');
                        return;
                    }
                    clientes.forEach(function (c) {
                        var btn = document.createElement('button');
                        btn.type = 'button';
                        btn.className = 'list-group-item list-group-item-action d-flex justify-content-between';
                        btn.innerHTML = '<span><i class="bi bi-person me-1"></i>' + (c.nombre || '') + '</span><small class="text-muted">' + (c.cedula || '') + '</small>';
                        btn.addEventListener('click', function () { seleccionarCliente(c); });
                        sugerencias.appendChild(btn);
                    });
                    sugerencias.classList.remove('d-none');
                });
        });

        function seleccionarCliente(c) {
            clienteIdHidden.value = c.idUsuario;
            inputNombre.value = c.nombre || '';
            inputCedula.value = c.cedula || '';
            inputNombre.readOnly = true;
            inputCedula.readOnly = true;
            clienteSelNombre.textContent = c.nombre || '';
            clienteSelCedula.textContent = c.cedula || 'No registrada';
            clienteSeleccionado.classList.remove('d-none');
            sugerencias.classList.add('d-none');
            sugerencias.innerHTML = '';
            busquedaInput.value = c.nombre && c.cedula ? (c.nombre + ' - ' + c.cedula) : (c.nombre || c.cedula || '');
        }

        document.addEventListener('click', function (e) {
            if (!busquedaSection.contains(e.target)) {
                sugerencias.classList.add('d-none');
            }
            var contR = buscadorRepuesto.parentElement;
            if (!contR.contains(e.target)) {
                sugerenciasRepuesto.classList.add('d-none');
            }
        });

        /* --- Precio del Servicio (solo números, sin símbolos ni puntos) --- */
        var monto = precioServicioInput.getAttribute('data-monto-input');
        if (monto === null) {
            precioServicioInput.setAttribute('data-monto-input', 'true');
        }

        var MIN_MONTO_CARACTERES = 4;
        var MAX_MONTO_CARACTERES = 11;

        function precioComoNumero() {
            var crudo = String(precioServicioInput.value || '').replace(/[^0-9]/g, '');
            if (crudo === '') {
                return 0;
            }
            var n = parseFloat(crudo);
            return isNaN(n) ? 0 : n;
        }

        // Solo se ingresan dígitos; cualquier otro carácter se descarta.
        precioServicioInput.addEventListener('input', function () {
            var soloDigitos = String(this.value || '').replace(/[^0-9]/g, '');
            if (soloDigitos.length > MAX_MONTO_CARACTERES) {
                soloDigitos = soloDigitos.substring(0, MAX_MONTO_CARACTERES);
            }
            if (this.value !== soloDigitos) {
                this.value = soloDigitos;
            }
            precioServicioVal.value = (soloDigitos === '' ? '0' : soloDigitos);
            recalcularTotal();
        });

        // Al salir del campo se conserva el valor ingresado (solo dígitos).
        precioServicioInput.addEventListener('blur', function () {
            var n = precioComoNumero();
            precioServicioVal.value = n.toFixed(2);
            recalcularTotal();
        });

        /* --- Repuestos --- */
        fetch('/caja/api/productos')
            .then(function (r) { return r.json(); })
            .then(function (p) {
                productosDisponibles = p || [];
            });

        function escaparHtml(s) {
            return String(s).replace(/[&<>"']/g, function (c) {
                return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
            });
        }

        function recalcularTotal() {
            var total = handlePrecioParse(precioServicioVal.value);
            repuestosSel.forEach(function (r) {
                total += (r.precio * r.cantidad);
            });
            totalServicioEl.textContent = moneda.formato(total);
        }

        function handlePrecioParse(v) {
            var n = parseFloat(v);
            return isNaN(n) ? 0 : n;
        }

        buscadorRepuesto.addEventListener('input', function () {
            var t = this.value.trim().toLowerCase();
            sugerenciasRepuesto.innerHTML = '';
            if (t.length < 2) {
                sugerenciasRepuesto.classList.add('d-none');
                return;
            }
            var filtrados = productosDisponibles.filter(function (p) {
                return (p.nombre && p.nombre.toLowerCase().includes(t)) ||
                    (p.marca && p.marca.toLowerCase().includes(t)) ||
                    (p.codigoBarras && p.codigoBarras.toLowerCase().includes(t));
            }).slice(0, 8);
            if (filtrados.length === 0) {
                sugerenciasRepuesto.innerHTML = '<button type="button" class="list-group-item list-group-item-action disabled">Sin resultados</button>';
                sugerenciasRepuesto.classList.remove('d-none');
                return;
            }
            filtrados.forEach(function (p) {
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center';
                var precio = (p.precioTotal != null ? p.precioTotal : (p.precio != null ? p.precio : 0));
                btn.innerHTML = '<span><strong>' + escaparHtml(p.nombre || '') + '</strong><br><small class="text-muted">' +
                    escaparHtml(p.marca || '') + ' · ' + ((p.stock != null) ? ('Stock: ' + p.stock) : 'Servicio') +
                    '</small></span><span class="text-success fw-bold">' + moneda.formato(precio) + '</span>';
                btn.addEventListener('click', function () {
                    agregarRepuesto(p);
                    buscadorRepuesto.value = '';
                    sugerenciasRepuesto.classList.add('d-none');
                });
                sugerenciasRepuesto.appendChild(btn);
            });
            sugerenciasRepuesto.classList.remove('d-none');
        });

        function precioRepuesto(producto) {
            if (producto.precioTotal != null) return producto.precioTotal;
            if (producto.precio != null) return producto.precio;
            return 0;
        }

        function agregarRepuesto(producto) {
            var precio = precioRepuesto(producto);
            if (producto.stock != null && producto.stock <= 0) {
                alert('No hay stock disponible para este repuesto.');
                return;
            }
            repuestosSel.push({
                id: producto.idProducto,
                nombre: producto.nombre,
                precio: precio,
                cantidad: 1,
                stockMax: producto.stock
            });
            renderRepuestos();
        }

        function renderRepuestos() {
            tablaRepuestos.innerHTML = '';
            if (repuestosSel.length === 0) {
                tablaRepuestos.innerHTML = '<tr id="filaRepuestoVacia"><td colspan="5" class="text-center text-muted py-3">No se han agregado repuestos.</td></tr>';
            } else {
                repuestosSel.forEach(function (r, idx) {
                    var tr = document.createElement('tr');
                    tr.setAttribute('data-fila', idx);
                    tr.innerHTML =
                        '<td class="fw-bold">' + escaparHtml(r.nombre) +
                        '<input type="hidden" name="repuestoProductoId" value="' + r.id + '"></td>' +
                        '<td><input type="number" min="1" class="form-control form-control-sm repuestoCantidad" name="repuestoCantidad" value="' + r.cantidad + '"></td>' +
                        '<td><input type="text" class="form-control form-control-sm repuestoPrecioInput" data-monto-input="true" value="' + moneda.numeroConComa(r.precio) +
                        '"><input type="hidden" name="repuestoPrecio" value="' + r.precio.toFixed(2) + '"></td>' +
                        '<td class="text-end fw-bold repuestoSubtotal">' + moneda.formato(r.precio * r.cantidad) + '</td>' +
                        '<td class="text-center"><button type="button" class="btn btn-sm btn-outline-danger quitarRepuestoBtn"><i class="bi bi-trash"></i></button></td>';
                    tablaRepuestos.appendChild(tr);
                });
            }
            recalcularTotal();
        }

        /* Delegación de eventos sobre la tabla (elimina onclick/onchange del HTML) */
        tablaRepuestos.addEventListener('input', function (e) {
            var target = e.target;
            if (target.classList && target.classList.contains('repuestoPrecioInput')) {
                // Escritura libre: solo se actualiza el valor numérico al vuelo.
                var idx = filaIndex(target);
                var r = repuestosSel[idx];
                if (r) {
                    var digitos = String(target.value || '').replace(/[^0-9]/g, '');
                    var n = parseFloat(digitos) || 0;
                    actualizarRepuesto(idx, n);
                }
            } else if (target.classList && target.classList.contains('repuestoCantidad')) {
                var idxC = filaIndex(target);
                cambiarCantidad(idxC, target.value);
            }
        });

        tablaRepuestos.addEventListener('focusout', function (e) {
            var target = e.target;
            if (target.classList && target.classList.contains('repuestoPrecioInput')) {
                var idx = filaIndex(target);
                var r = repuestosSel[idx];
                if (r) {
                    var n = moneda.parse(target.value);
                    target.value = n > 0 ? moneda.numeroConComa(n) : '0,00';
                    actualizarRepuesto(idx, n);
                }
            }
        }, true);

        tablaRepuestos.addEventListener('click', function (e) {
            var btn = e.target.closest('.quitarRepuestoBtn');
            if (btn) {
                var idx = filaIndex(btn);
                repuestosSel.splice(idx, 1);
                renderRepuestos();
            }
        });

        function filaIndex(elm) {
            var tr = elm.closest('tr[data-fila]');
            return tr ? parseInt(tr.getAttribute('data-fila'), 10) : -1;
        }

        function actualizarRepuesto(idx, precio) {
            if (!repuestosSel[idx]) return;
            repuestosSel[idx].precio = precio;
            actualizarSubtotal(idx);
        }

        function cambiarCantidad(idx, v) {
            var cant = parseInt(v, 10) || 1;
            var r = repuestosSel[idx];
            if (!r) return;
            if (r.stockMax != null && cant > r.stockMax) {
                alert('El stock máximo disponible es ' + r.stockMax);
                r.cantidad = r.stockMax;
            } else {
                r.cantidad = cant;
            }
            renderRepuestos();
        }

        function actualizarSubtotal(idx) {
            var r = repuestosSel[idx];
            var fila = tablaRepuestos.querySelector('tr[data-fila="' + idx + '"]');
            if (fila && r) {
                var tdSub = fila.querySelector('.repuestoSubtotal');
                var hidden = fila.querySelector('input[name="repuestoPrecio"]');
                if (hidden) hidden.value = r.precio.toFixed(2);
                if (tdSub) tdSub.textContent = moneda.formato(r.precio * r.cantidad);
            }
            recalcularTotal();
        }

        if (btnAgregarRepuesto) {
            btnAgregarRepuesto.addEventListener('click', function () {
                buscadorRepuesto.dispatchEvent(new Event('input'));
            });
        }

        setModo('registrado');
        recalcularTotal();
    }

    document.addEventListener('DOMContentLoaded', initRegistroServicio);
})(window);
