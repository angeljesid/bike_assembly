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
                var sinSimbolo = numero.toFixed(2).replace('.', ',');
                var partes = sinSimbolo.split(',');
                partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                input.value = partes.join(',');
            }
        });
        input.addEventListener('blur', function () {
            if (input.value) {
                var n = parseMoneda(input.value);
                var sinSimbolo = n.toFixed(2).replace('.', ',');
                var partes = sinSimbolo.split(',');
                partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                input.value = partes.join(',');
            }
        });
    }

    window.moneda = {
        formato: formatoMoneda,
        parse: parseMoneda,
        formatearInput: formatearInput,
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
})(window);
