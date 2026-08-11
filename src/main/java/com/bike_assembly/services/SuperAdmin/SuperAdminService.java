package com.bike_assembly.services.SuperAdmin;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SuperAdminService implements ISuperAdminService {

    @Autowired
    private OrganizacionDao organizacionDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario crearAdminConOrganizacion(String nombreOrganizacion, Usuario nuevoAdmin) {
        Organizacion org = new Organizacion(nombreOrganizacion);
        org = organizacionDao.save(org);

        nuevoAdmin.setPassword(passwordEncoder.encode(nuevoAdmin.getPassword()));
        nuevoAdmin.setRol(Rol.ADMIN);
        nuevoAdmin.setOrganizacion(org);

        return usuarioDao.save(nuevoAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAdministradores() {
        return usuarioDao.findByRol(Rol.ADMIN);
    }
}
