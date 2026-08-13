package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao; // Tu repositorio de usuarios (asumo que se llama así)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuperAdminServiceImpl implements ISuperAdminService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Override
    public List<Usuario> listarAdministradores() {
        /* 
         * NOTA: Aquí lo ideal es retornar solo los usuarios que tengan el ROL de "ADMIN".
         * Por ahora, para que compile y no te dé error, traeremos todos los usuarios.
         * Más adelante lo ajustamos según cómo tengas configurados los roles en tu base de datos.
         */
        return (List<Usuario>) usuarioDao.findAll(); 
    }
}