package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.OtpVerificacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OtpVerificacionDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class OtpService {

    private static final Set<Rol> ROLES_CON_OTP = Set.of(Rol.SUPER_ADMIN, Rol.ADMIN, Rol.CAJA, Rol.OPERARIO);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private OtpVerificacionDao otpDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean requiereOtp(Rol rol) {
        return rol != null && ROLES_CON_OTP.contains(rol);
    }

    /**
     * Devuelve true si el usuario ya tiene un código OTP vigente (emitido hace menos de 24h).
     */
    private boolean tieneOtpVigente(Long idUsuario) {
        Optional<OtpVerificacion> opt = otpDao.findByIdUsuario(idUsuario);
        return opt.isPresent()
                && opt.get().getFechaVencimiento() != null
                && opt.get().getFechaVencimiento().isAfter(LocalDateTime.now());
    }

    @Transactional
    public boolean generarOEnviarOtp(Usuario usuario) {
        Optional<OtpVerificacion> existente = otpDao.findByIdUsuario(usuario.getIdUsuario());
        if (existente.isPresent() && existeVigente(existente.get())) {
            return false;
        }
        if (existente.isPresent()) {
            otpDao.delete(existente.get());
        }

        String codigo = String.valueOf(100000 + RANDOM.nextInt(900000)); // 6 dígitos
        OtpVerificacion nuevo = new OtpVerificacion();
        nuevo.setIdUsuario(usuario.getIdUsuario());
        nuevo.setCodigo(passwordEncoder.encode(codigo));
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setFechaVencimiento(LocalDateTime.now().plusHours(OtpVerificacion.VALIDEZ_HORAS));
        otpDao.save(nuevo);

        String cuerpo = "Hola " + ((usuario.getNombre() != null && !usuario.getNombre().isBlank())
                ? usuario.getNombre() : "usuario")
                + ",\n\n"
                + "Tu código de verificación para acceder a Bike Assembly es:\n\n"
                + "   " + codigo + "\n\n"
                + "Este código es válido por 24 horas. No lo compartas con nadie.\n\n"
                + "Bike Assembly";
        emailService.enviar(usuario.getEmail(), "Tu código de acceso - Bike Assembly", cuerpo);
        return true;
    }

    private boolean existeVigente(OtpVerificacion otp) {
        return otp.getFechaVencimiento() != null && otp.getFechaVencimiento().isAfter(LocalDateTime.now());
    }


    @Transactional(readOnly = true)
    public boolean validarOtp(Usuario usuario, String codigo) {
        if (usuario == null || codigo == null) {
            return false;
        }
        Optional<OtpVerificacion> opt = otpDao.findByIdUsuario(usuario.getIdUsuario());
        if (opt.isEmpty() || !existeVigente(opt.get())) {
            return false;
        }
        return passwordEncoder.matches(codigo.trim(), opt.get().getCodigo());
    }
}
