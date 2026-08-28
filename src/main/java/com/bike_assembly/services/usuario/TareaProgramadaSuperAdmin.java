package com.bike_assembly.services.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareaProgramadaSuperAdmin {

    private static final Logger log = LoggerFactory.getLogger(TareaProgramadaSuperAdmin.class);

    @Autowired
    private ISuperAdminService superAdminService;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgarCuentasHuerfanasDiariamente() {
        int purgadas = superAdminService.purgarCuentasHuerfanas(7);
        if (purgadas > 0) {
            log.info("Tarea programada: se purgaron {} cuentas de administradores huérfanas (sin taller) tras 7 días.", purgadas);
        }
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void sincronizarVencimientosDiariamente() {
        superAdminService.sincronizarTalleresVencidos();
    }
}
