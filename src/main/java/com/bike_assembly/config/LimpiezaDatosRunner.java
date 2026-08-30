package com.bike_assembly.config;

import com.bike_assembly.domain.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Limpieza de datos al arranque: borra todos los registros de la base de datos
 * (usuarios, organizaciones, productos, proveedores, ventas, órdenes, solicitudes)
 * dejando ÚNICAMENTE la cuenta de SUPER_ADMIN. La estructura de tablas, roles y
 * funcionalidades no se toca. Se activa con la propiedad app.limpiar-datos=true.
 */
@Component
@Order(1)
public class LimpiezaDatosRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaDatosRunner.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        String limpiar = System.getenv().containsKey("LIMPIAR_DATOS")
                ? System.getenv("LIMPIAR_DATOS")
                : "";
        boolean activo = Boolean.parseBoolean(limpiar) || "true".equalsIgnoreCase(
                System.getProperty("app.limpiar-datos", "false"));

        if (!activo) {
            return;
        }

        log.warn("=== LIMPIEZA DE DATOS INICIADA: se borrarán todos los registros excepto el SUPER_ADMIN ===");

        em.createQuery("DELETE FROM RepuestoOrdenServicio").executeUpdate();
        em.createQuery("DELETE FROM SolicitudRepuesto").executeUpdate();
        em.createQuery("DELETE FROM DetalleVenta").executeUpdate();
        em.createQuery("DELETE FROM OrdenServicio").executeUpdate();
        em.createQuery("DELETE FROM Venta").executeUpdate();
        em.createQuery("DELETE FROM Producto").executeUpdate();
        em.createQuery("DELETE FROM Proveedor").executeUpdate();
        em.createQuery("DELETE FROM Organizacion").executeUpdate();

        // Borrar todos los usuarios excepto el SUPER_ADMIN
        em.createQuery("DELETE FROM Usuario u WHERE u.rol <> :superAdmin")
                .setParameter("superAdmin", Rol.SUPER_ADMIN)
                .executeUpdate();

        log.warn("=== LIMPIEZA DE DATOS COMPLETADA: queda únicamente el SUPER_ADMIN ===");
    }
}
