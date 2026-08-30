package com.bike_assembly.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    /**
     * Envía un correo simple. Si no hay servidor de correo configurado (mailSender nulo
     * o sin credenciales), lo registra en el log y devuelve false sin fallar.
     */
    public boolean enviar(String para, String asunto, String cuerpo) {
        if (mailSender == null || remitente == null || remitente.isBlank()) {
            log.warn("Correo no enviado a '{}' (asunto '{}'): servidor SMTP no configurado. Cuerpo: {}",
                    para, asunto, cuerpo);
            return false;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(para);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            return true;
        } catch (Exception e) {
            log.error("Error enviando correo a '{}': {}", para, e.getMessage());
            return false;
        }
    }
}
