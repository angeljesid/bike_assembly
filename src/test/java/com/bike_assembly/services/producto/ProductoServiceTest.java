package com.bike_assembly.services.producto;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.repository.ProductoDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoDao productoDao;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void listarProductosTest() {
        List<Producto> listaEsperada = Arrays.asList(new Producto(), new Producto());
        when(productoDao.findAll()).thenReturn(listaEsperada);

        List<Producto> resultado = productoService.listarProductos();

        assertEquals(2, resultado.size());
        verify(productoDao).findAll();
    }

    @Test
    void obtenerCompatiblesTest() {
        String tipo = "FRENOS";
        List<Producto> listaEsperada = Arrays.asList(new Producto());
        when(productoDao.findComponentesCompatibles(tipo)).thenReturn(listaEsperada);

        List<Producto> resultado = productoService.obtenerCompatibles(tipo);

        assertEquals(1, resultado.size());
        verify(productoDao).findComponentesCompatibles(tipo);
    }

    @Test
    void guardarTest() {
        Producto producto = new Producto();

        productoService.guardar(producto);

        verify(productoDao).save(producto);
    }

    @Test
    void eliminarTest() {
        Producto producto = new Producto();

        productoService.eliminar(producto);

        verify(productoDao).delete(producto);
    }

    @Test
    void encontrarProductoTest() {
        Producto producto = new Producto();
        producto.setIdProducto(1L); 
        
        when(productoDao.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.encontrarProducto(producto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdProducto());
        verify(productoDao).findById(1L);
    }
}