package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionPasswordService {

    private static final long VALIDEZ_HORAS = 1;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public boolean solicitarRecuperacion(String email) {
        String correo = email == null ? "" : email.trim().toLowerCase();
        Optional<Usuario> opt = usuarioDao.findByEmail(correo);
        if (opt.isEmpty()) {
            return false;
        }
        Usuario u = opt.get();
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        u.setResetPasswordToken(token);
        u.setTokenExpirationDate(LocalDateTime.now().plusHours(VALIDEZ_HORAS));
        usuarioDao.save(u);

        String enlace = baseUrl + "/recuperar-password/reset?token=" + token;
        String cuerpo = "Hola " + ((u.getNombre() != null && !u.getNombre().isBlank()) ? u.getNombre() : "usuario")
                + ",\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña en Bike Assembly.\n\n"
                + "Haz clic en el siguiente enlace (válido por " + VALIDEZ_HORAS + " hora):\n"
                + enlace + "\n\n"
                + "Si no solicitaste este cambio, ignora este correo.\n\n"
                + "Bike Assembly";
        emailService.enviar(u.getEmail(), "Recuperación de contraseña - Bike Assembly", cuerpo);
        return true;
    }
    @Transactional(readOnly = true)
    public Optional<Usuario> validarToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Usuario> opt = usuarioDao.findByResetPasswordToken(token);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Usuario u = opt.get();
        if (u.getTokenExpirationDate() == null || u.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return opt;
    }

    @Transactional
    public boolean restablecerContrasena(String token, String nuevaContrasena) {
        Optional<Usuario> opt = validarToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        Usuario u = opt.get();
        u.setPassword(passwordEncoder.encode(nuevaContrasena));
        u.setResetPasswordToken(null);
        u.setTokenExpirationDate(null);
        usuarioDao.save(u);
        return true;
    }
}