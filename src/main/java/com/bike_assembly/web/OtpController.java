package com.bike_assembly.web;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.security.AtributosSesion;
import com.bike_assembly.security.RedirectPorRol;
import com.bike_assembly.services.usuario.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OtpController {

    private static final int MAX_INTENTOS = 5;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UsuarioDao usuarioDao;

    private Usuario obtenerUsuario(String username) {
        return username == null ? null
                : usuarioDao.findByUsernameOrEmail(username, username).orElse(null);
    }

    @GetMapping("/verificar-otp")
    public String mostrarVerificacion(jakarta.servlet.http.HttpServletRequest request,
                                      org.springframework.security.core.Authentication authentication,
                                      Model model) {
        Usuario usuario = obtenerUsuario(authentication == null ? null : authentication.getName());
        if (usuario == null) {
            return "redirect:/login";
        }

        // Ya verificado previamente en esta sesión
        if (Boolean.TRUE.equals(request.getSession().getAttribute(AtributosSesion.OTP_VERIFICADO))) {
            return redirigirSegunRol(usuario.getRol());
        }

        boolean existeVigente = otpService.existeOtpVigente(usuario.getIdUsuario());
        boolean correoEnviado = existeVigente || otpService.generarOEnviarOtp(usuario);
        model.addAttribute("correoEnviado", correoEnviado);
        model.addAttribute("nombre", usuario.getNombre());
        return "verificar-otp";
    }

    @PostMapping("/verificar-otp")
    public String verificar(jakarta.servlet.http.HttpServletRequest request,
                            org.springframework.security.core.Authentication authentication,
                            @RequestParam("codigo") String codigo,
                            Model model) {
        Usuario usuario = obtenerUsuario(authentication == null ? null : authentication.getName());
        if (usuario == null) {
            return "redirect:/login";
        }

        Integer intentos = (Integer) request.getSession().getAttribute("otpIntentosFallidos");
        if (intentos == null) {
            intentos = 0;
        }
        if (intentos >= MAX_INTENTOS) {
            request.getSession().setAttribute("otpIntentosFallidos", intentos);
            model.addAttribute("error", "Demasiados intentos fallidos. Contacta al administrador del sistema.");
            model.addAttribute("nombre", usuario.getNombre());
            return "verificar-otp";
        }

        if (otpService.validarOtp(usuario, codigo)) {
            request.getSession().setAttribute(AtributosSesion.OTP_VERIFICADO, Boolean.TRUE);
            request.getSession().removeAttribute("otpIntentosFallidos");
            return redirigirSegunRol(usuario.getRol());
        }

        intentos++;
        request.getSession().setAttribute("otpIntentosFallidos", intentos);
        model.addAttribute("error", "Código incorrecto. Te quedan "
                + (MAX_INTENTOS - intentos) + " intentos.");
        model.addAttribute("nombre", usuario.getNombre());
        return "verificar-otp";
    }

    private String redirigirSegunRol(Rol rol) {
        return "redirect:" + RedirectPorRol.urlDestino(rol);
    }

    @PostMapping("/verificar-otp/reenviar")
    public String reenviar(jakarta.servlet.http.HttpServletRequest request,
                           org.springframework.security.core.Authentication authentication,
                           Model model) {
        Usuario usuario = obtenerUsuario(authentication == null ? null : authentication.getName());
        if (usuario == null) {
            return "redirect:/login";
        }

        boolean correoEnviado = otpService.reenviarCodigo(usuario);
        request.getSession().removeAttribute("otpIntentosFallidos");
        model.addAttribute("correoEnviado", correoEnviado);
        model.addAttribute("nombre", usuario.getNombre());
        return "verificar-otp";
    }
}
