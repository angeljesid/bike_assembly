package com.bike_assembly.web;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Locale;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, Model model) {

        String error = validarRegistro(usuario);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("usuario", usuario);
            return "registro";
        }

        usuario.setRol(Rol.CLIENTE);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setEmail(usuario.getEmail() != null ? usuario.getEmail().trim().toLowerCase(Locale.ROOT) : null);
        usuarioDao.save(usuario);

        return "redirect:/login?registrado=true";
    }

    private String validarRegistro(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return "Debes completar el nombre de usuario y el correo.";
        }

        String username = usuario.getUsername().trim();
        String email = usuario.getEmail().trim().toLowerCase(Locale.ROOT);

        // Duplicados: evitar fuga de existencia, pero avisar en el propio formulario.
        if (usuarioDao.findByUsername(username).isPresent()) {
            return "El nombre de usuario ya está en uso. Elige otro.";
        }
        if (usuarioDao.findByEmail(email).isPresent()) {
            return "El correo ya está registrado. Inicia sesión o usa otro correo.";
        }
        if (usuario.getCedula() != null && !usuario.getCedula().isBlank()
                && usuarioDao.findByCedula(usuario.getCedula().trim()).isPresent()) {
            return "La cédula ya está registrada.";
        }

        String errorPolitica = PasswordValidator.validar(usuario.getPassword());
        if (errorPolitica != null) {
            return errorPolitica;
        }

        return null;
    }
}