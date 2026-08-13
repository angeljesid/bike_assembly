package com.bike_assembly.config;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioDao.existsByRol(Rol.SUPER_ADMIN)) {
            Usuario superAdmin = new Usuario();
            superAdmin.setUsername("superadmin");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setNombre("Super Administrador Global");
            superAdmin.setEmail("superadmin@bikeassembly.com");
            superAdmin.setRol(Rol.SUPER_ADMIN);
            superAdmin.setOrganizacion(null);
            usuarioDao.save(superAdmin);
            System.out.println(">>> Super Admin creado con éxito: Username 'superadmin' / Clave 'superadmin123'");
            
        }
    }
}