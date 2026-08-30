package com.bike_assembly.security;

import com.bike_assembly.domain.Rol;

public final class RedirectPorRol {

    private RedirectPorRol() {
    }

    public static String urlDestino(Rol rol) {
        if (rol == null) {
            return "/";
        }
        switch (rol) {
            case SUPER_ADMIN: return "/superadmin/dashboard";
            case ADMIN:       return "/admin";
            case CAJA:        return "/caja/pos";
            case OPERARIO:    return "/operario/ensambles";
            case CLIENTE:     return "/cliente/catalogo";
            default:          return "/";
        }
    }
}
