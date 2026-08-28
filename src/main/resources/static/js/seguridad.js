/* seguridad.js — Sanitización global de entradas (anti-inyección de datos)
 * Se aplica a todos los campos de texto y búsqueda de la aplicación desde el cliente.
 * No sustituye el uso de PreparedStatement/JPA del backend (Spring Data ya escapa),
 * pero bloquea caracteres maliciosos y payloads antes de que lleguen al servidor.
 */
(function () {
    'use strict';

    /* Caracteres peligrosos que se eliminan de cualquier texto ingresado para
       evitar inyección de scripts, comandos, SQL o marcado HTML/XSS. */
    var CARACTERES_PELIGROSOS = /[<>'";`{}[\]\\]/g;

    /* Longitud máxima para los campos de búsqueda. */
    var MAX_BUSQUEDA = 25;

    /* Longitud máxima genérica para campos de texto libre. */
    var MAX_TEXTO = 100;

    function esCampoBusqueda(el) {
        var id = el.id || '';
        var cls = el.className || '';
        return /buscar|buscador|search|busqueda/i.test(id + ' ' + cls);
    }

    function esCampoCodigoBarras(el) {
        var id = el.id || '';
        var name = el.name || '';
        return /codigo|barras/i.test(id + ' ' + name);
    }

    /* Limpia un valor eliminando caracteres peligrosos y aplicando límite. */
    function limpiarValor(el) {
        var valor = el.value;
        if (valor == null) { return; }

        var limpio = valor.replace(CARACTERES_PELIGROSOS, '');

        var max = MAX_TEXTO;
        if (esCampoBusqueda(el)) {
            max = MAX_BUSQUEDA;
        } else if (esCampoCodigoBarras(el)) {
            // Código de barras: solo dígitos.
            limpio = limpio.replace(/[^0-9]/g, '');
            max = 15;
        } else if (el.type === 'email') {
            // Email: quitar espacios blancos (el resto ya se sanitiza).
            limpio = limpio.replace(/\s/g, '');
            max = 100;
        } else if (el.maxLength) {
            max = el.maxLength;
        }

        if (limpio.length > max) {
            limpio = limpio.substring(0, max);
        }

        if (el.value !== limpio) {
            el.value = limpio;
        }
    }

    /* Previene la pulsación de caracteres peligrosos en tiempo real (beforeinput).
       NUNCA interviene en campos password: las contraseñas pueden contener cualquier
       símbolo y se almacenan hasheadas (BCrypt), no se interpolan en SQL. */
    function alAntesDeInsertar(ev) {
        var el = ev.target;
        var tipo = (el.type || '').toLowerCase();
        if (tipo === 'password') { return; }
        // Solo se aplica a campos de texto editables.
        if (!/^(text|search|email|tel|url)$/.test(tipo) && el.tagName !== 'TEXTAREA') {
            return;
        }
        if (ev.data && CARACTERES_PELIGROSOS.test(ev.data)) {
            CARACTERES_PELIGROSOS.lastIndex = 0;
            ev.preventDefault();
        }
    }

    function alEntrada(ev) {
        var el = ev.target;
        var tipo = (el.type || '').toLowerCase();
        if (tipo === 'password') { return; }
        if (!/^(text|search|email|tel|url)$/.test(tipo) && el.tagName !== 'TEXTAREA') {
            return;
        }
        limpiarValor(el);
    }

    /* Protección a nivel documento, cubre elementos futuros/agregados dinámicamente. */
    document.addEventListener('beforeinput', alAntesDeInsertar, true);
    document.addEventListener('input', alEntrada, true);

    /* Sanitiza todo campo ya presente en la página al cargar. */
    function sanearExistentes() {
        var campos = document.querySelectorAll('input[type="text"], input[type="search"], ' +
            'input[type="email"], input[type="tel"], input[type="url"], textarea');
        for (var i = 0; i < campos.length; i++) {
            var el = campos[i];
            // Nunca se limita un campo password.
            if ((el.type || '').toLowerCase() === 'password') { continue; }
            if (esCampoBusqueda(el) && !el.maxLength) {
                el.maxLength = MAX_BUSQUEDA;
            }
            // Si el campo no define un maxlength propio de negocio, aplicamos el genérico.
            if (!el.maxLength) {
                el.maxLength = esCampoBusqueda(el) ? MAX_BUSQUEDA : MAX_TEXTO;
            }
        }
    }

    document.addEventListener('DOMContentLoaded', sanearExistentes);
})();
