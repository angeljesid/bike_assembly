package com.bike_assembly.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Limpieza de datos al arranque: borra todos los registros de la base de datos
 * (usuarios, organizaciones, productos, proveedores, ventas, órdenes, solicitudes, OTP)
 * y las imágenes subidas, dejando ÚNICAMENTE la cuenta de SUPER_ADMIN. La estructura
 * de tablas, roles y funcionalidades no se toca. Se activa con app.limpiar-datos=true.
 */
@Component
@Order(1)
public class LimpiezaDatosRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaDatosRunner.class);

    @PersistenceContext
    private EntityManager em;

    @Value("${app.limpiar-datos:false}")
    private boolean limpiarDatos;

    @Override
    @Transactional
    public void run(String... args) {
        if (!limpiarDatos) {
            return;
        }

        log.warn("=== LIMPIEZA DE DATOS INICIADA: se borrarán todos los registros incluyendo el SUPER_ADMIN ===");

        em.createQuery("DELETE FROM RepuestoOrdenServicio").executeUpdate();
        em.createQuery("DELETE FROM SolicitudRepuesto").executeUpdate();
        em.createQuery("DELETE FROM DetalleVenta").executeUpdate();
        em.createQuery("DELETE FROM OrdenServicio").executeUpdate();
        em.createQuery("DELETE FROM Venta").executeUpdate();
        em.createQuery("DELETE FROM ProductoGuardado").executeUpdate();
        em.createQuery("DELETE FROM Producto").executeUpdate();
        em.createQuery("DELETE FROM Proveedor").executeUpdate();
        em.createQuery("DELETE FROM OtpVerificacion").executeUpdate();

        // Borrar TODOS los usuarios (incluido el superadmin); será re-creado por DataInitializer
        em.createQuery("DELETE FROM Usuario").executeUpdate();

        // Las organizaciones se borran al final: hay FKs desde usuario, producto, etc.
        em.createQuery("DELETE FROM Organizacion").executeUpdate();

        // Limpiar las imágenes de productos subidas (datos de ejemplo)
        limpiarDirectorioUploads();

        log.warn("=== LIMPIEZA DE DATOS COMPLETADA: todos los registros eliminados; el SUPER_ADMIN se re-creará al siguiente paso ===");
    }

    private void limpiarDirectorioUploads() {
        Path dir = Paths.get("uploads");
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> arhivos = Files.list(dir)) {
            arhivos.forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("No se pudo borrar el archivo temporal '{}'", p);
                }
            });
        } catch (IOException e) {
            log.warn("No se pudo limpiar el directorio de uploads: {}", e.getMessage());
        }
    }
}
