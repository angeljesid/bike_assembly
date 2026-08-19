    document.addEventListener('DOMContentLoaded', function () {
        

        let tablaProductos = new DataTable('#tabla-inventario', {
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json',
            },
            pageLength: 10,
            ordering: true, 
        });

    });