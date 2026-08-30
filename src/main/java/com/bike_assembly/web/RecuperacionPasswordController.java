package com.bike_assembly.web;

import com.bike_assembly.services.PasswordValidator;
import com.bike_assembly.services.usuario.RecuperacionPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperacionPasswordController {

    @Autowired
    private RecuperacionPasswordService recuperacionPasswordService;

    @GetMapping("/recuperar-password")
    public String mostrarSolicitud() {
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarSolicitud(@RequestParam("email") String email, Model model) {
        // Mensaje genérico para no revelar qué correos existen (evita enumeración de cuentas)
        recuperacionPasswordService.solicitarRecuperacion(email);
        model.addAttribute("enviado", true);
        return "recuperar-password";
    }

    @GetMapping("/recuperar-password/reset")
    public String mostrarReset(@RequestParam("token") String token, Model model) {
        boolean valido = recuperacionPasswordService.validarToken(token).isPresent();
        if (!valido) {
            model.addAttribute("tokenInvalido", true);
            return "recuperar-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/recuperar-password/reset")
    public String procesarReset(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!recuperacionPasswordService.validarToken(token).isPresent()) {
            model.addAttribute("tokenInvalido", true);
            return "recuperar-password";
        }
        if (password == null || !password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "reset-password";
        }
        String errorPolitica = PasswordValidator.validar(password);
        if (errorPolitica != null) {
            model.addAttribute("token", token);
            model.addAttribute("error", errorPolitica);
            return "reset-password";
        }
        recuperacionPasswordService.restablecerContrasena(token, password);
        return "redirect:/login?reset=exito";
    }
}
