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

        if (usuarioDao.findByEmail("admintaller@bikeassembly.com").isEmpty()) {
            Usuario adminTaller = new Usuario();
            adminTaller.setUsername("admin_taller");
            adminTaller.setNombre("Dueño de Taller");
            adminTaller.setEmail("admintaller@bikeassembly.com");
            adminTaller.setPassword(passwordEncoder.encode("12345")); // Tu contraseña deseada
            adminTaller.setRol(Rol.ADMIN);
            usuarioDao.save(adminTaller);
            System.out.println("-> Admin de taller creado exitosamente.");
        }

        if (usuarioDao.findByEmail("mecanico@taller.com").isEmpty()) {
            Usuario operario = new Usuario();
            operario.setUsername("mecanico1");
            operario.setNombre("Mecánico Principal");
            operario.setEmail("mecanico@taller.com");
            operario.setPassword(passwordEncoder.encode("12345"));
            operario.setRol(Rol.OPERARIO);
            usuarioDao.save(operario);
            System.out.println("-> Operario creado exitosamente.");
        }

        if (usuarioDao.findByEmail("cliente@correo.com").isEmpty()) {
            Usuario cliente = new Usuario();
            cliente.setUsername("cliente1");
            cliente.setNombre("Juan Cliente");
            cliente.setEmail("cliente@correo.com");
            cliente.setPassword(passwordEncoder.encode("12345"));
            cliente.setRol(Rol.CLIENTE);
            usuarioDao.save(cliente);
            System.out.println("-> Cliente creado exitosamente.");
        }
    }
}