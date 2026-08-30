package com.bike_assembly.config;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garantiza la existencia de una cuenta SUPER_ADMIN. Se ejecuta DESPUÉS del
 * LimpiezaDatosRunner (@Order 1). Las credenciales se leen de variables de
 * entorno/propiedades para no exponer la contraseña en el código fuente.
 */
@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${SUPERADMIN_USERNAME:comezaquira2005@gmail.com}")
    private String superAdminUsername;

    @Value("${SUPERADMIN_EMAIL:comezaquira2005@gmail.com}")
    private String superAdminEmail;

    @Value("${SUPERADMIN_PASSWORD:}")
    private String superAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioDao.existsByRol(Rol.SUPER_ADMIN)) {
            return;
        }

        if (superAdminPassword == null || superAdminPassword.isBlank()) {
            throw new IllegalStateException(
                    "No existe ningún SUPER_ADMIN y no se proporcionó la variable de entorno/la propiedad " +
                    "SUPERADMIN_PASSWORD. Defínela para poder crear la cuenta de superadministrador.");
        }

        Usuario superAdmin = new Usuario();
        superAdmin.setUsername(superAdminUsername.trim());
        superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
        superAdmin.setNombre("Super Administrador Global");
        superAdmin.setEmail(superAdminEmail.trim());
        superAdmin.setRol(Rol.SUPER_ADMIN);
        superAdmin.setOrganizacion(null);
        usuarioDao.save(superAdmin);
        log.info(">>> Super Admin generado: usuario '{}'", superAdminUsername);
    }
}
