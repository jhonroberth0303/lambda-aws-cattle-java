package com.cattle.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * Principal de seguridad que representa un usuario autenticado de una finca.
 * Se almacena en el SecurityContext después de validar el JWT.
 */
@Getter
@AllArgsConstructor
public class FarmUserPrincipal implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ID de la finca a la que pertenece el usuario.
     * Usado para filtrar datos por tenant (multi-tenancy).
     */
    private final String farmId;
    
    /**
     * ID único del usuario (subject del JWT).
     */
    private final String userId;
    
    @Override
    public String toString() {
        return String.format("FarmUserPrincipal[farmId=%s, userId=%s]", farmId, userId);
    }
}
