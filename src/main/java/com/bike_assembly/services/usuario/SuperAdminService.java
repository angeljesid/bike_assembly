package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuperAdminService implements ISuperAdminService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Override
    public List<Usuario> listarAdministradores() {
        return usuarioDao.findByRol(Rol.ADMIN);
    }

    @Override
    public List<Usuario> listarClientes() {
        return usuarioDao.findByRol(Rol.CLIENTE);
    }

    @Override
    @Transactional
    public void promoverAAdmin(Long idUsuario, String nombreTaller) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Organizacion nuevaOrg = new Organizacion();
        nuevaOrg.setNombre(nombreTaller);
        nuevaOrg.setDireccion("Pendiente de actualizar");
        nuevaOrg.setTelefono("0000000000");
        Organizacion orgGuardada = organizacionDao.save(nuevaOrg);

        usuario.setRol(Rol.ADMIN);
        usuario.setOrganizacion(orgGuardada);
        usuarioDao.save(usuario);
    }
}