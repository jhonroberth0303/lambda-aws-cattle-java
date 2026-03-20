package com.cattle.utils;

import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import com.cattle.events.MaintenanceSetEvent;
import com.cattle.events.PastureEvent;
import com.cattle.events.EntityPatch;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class PastureStatusEngine {

    /** Aplica un evento de negocio y devuelve un Patch con los cambios a persistir. */
    public EntityPatch applyEvent(Pasture pasture, Plan plan, PastureEvent ev) {
        EntityPatch entityPatch = EntityPatch.of();
        Instant now = Instant.now();
        switch (ev.type()) {
            case OPEN -> {
                // Reglas: no abrir si bloqueado efectivo
                if (isBlockedEffective(pasture)) {
                    throw new IllegalStateException("No se puede abrir: potrero bloqueado (" + pasture.getSubstatus() + ")");
                }
                entityPatch.set("status", PastureStatus.EN_USO.name());
                // Podrías registrar un evento aparte (GRAZING_START / CUT_START)
            }
            case CLOSE -> {
                // Cierre → descanso
                entityPatch.set("status", PastureStatus.EN_DESCANSO.name());
                entityPatch.set("lastUseAtIso", now.toString());
                // Podrías crear tarea de fertilización post-uso aquí (fuera del motor)
            }
            case MAINTENANCE_SET -> {
                MaintenanceSetEvent m = (MaintenanceSetEvent) ev;
                entityPatch.set("status", PastureStatus.MANTENIMIENTO.name());
                entityPatch.set("substatus", m.substatus().name());
                if (m.holdUntil() != null && !m.holdUntil().isBlank()) {
                    entityPatch.set("holdUntil", m.holdUntil());
                }
                // Activar índice de bloqueados (sparse)
                entityPatch.set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#true");
                // Si quieres ordenar por ETA dentro del GSI:
                int eta = EtaCalculator.etaOpenDays(pasture, plan);
                entityPatch.set("gsi2sk", Math.max(eta, 0));
            }
            case MAINTENANCE_CLEAR -> {
                entityPatch.set("substatus", PastureStatus.NINGUNO.name());
                entityPatch.remove("holdUntil");
                // Volver a estado según ETA actual
                int eta = EtaCalculator.etaOpenDays(pasture, plan);
                PastureStatus next = (eta <= 0) ? PastureStatus.DISPONIBLE : PastureStatus.EN_DESCANSO;
                entityPatch.set("status", next.name());
                // Remover del GSI de bloqueados
                entityPatch.remove("gsi2pk");
                entityPatch.remove("gsi2sk");
            }
        }
        return entityPatch;
    }

    /** Actualizacion automatica por holdUntil vencido. si esta holduntil esta vencido se desbloquea */
    public EntityPatch autoUpdateStatusTickByHoldUntil(Pasture pasture, Plan plan) {
        EntityPatch entityPatch = EntityPatch.of();

        // Si estaba en mantenimiento y holdUntil ya venció → limpiar
        if (PastureStatus.MANTENIMIENTO.name().equals(pasture.getStatus()) && isHoldUntilExpired(pasture)) {
            entityPatch.set("substatus", PastureSubstatus.NINGUNO.name());
            entityPatch.remove("holdUntil");
            // Recalcular estado por ETA
            int eta = EtaCalculator.etaOpenDays(pasture, plan);
            PastureStatus next = (eta <= 0) ? PastureStatus.DISPONIBLE : PastureStatus.EN_DESCANSO;
            entityPatch.set("status", next.name());
            entityPatch.set("gsi2pk", "farm#" + pasture.getFarmId() + "#blocked#false");
            entityPatch.remove("gsi2sk");
        }

        // (Opcional) si estaba EN_USO y reportas cierre automático por tiempo, podrías manejarlo aquí.

        return entityPatch;
    }

    /** Retorna true si holdUntil existe y ya venció (es anterior a ahora). */
    private boolean isHoldUntilExpired(Pasture pasture) {
        if (pasture.getHoldUntil() == null || pasture.getHoldUntil().isBlank()) {
            return false;
        }
        try {
            LocalDate holdDate = LocalDate.parse(pasture.getHoldUntil());
            Instant hu = holdDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            return Instant.now().isAfter(hu);
        } catch (Exception e) {
            return false;
        }
    }

    /** Estado “efectivo” para la UI en lectura (sin mutar almacenamiento). */
    public PastureStatus deriveEffectiveStatus(Pasture pasture, int etaOpenDays) {
        if (PastureStatus.EN_USO.name().equals(pasture.getStatus())) {
            return PastureStatus.EN_USO;
        }
        if (isBlockedEffective(pasture)) {
            return PastureStatus.MANTENIMIENTO;
        }
        return etaOpenDays <= 0 ? PastureStatus.DISPONIBLE : PastureStatus.EN_DESCANSO;
    }

    /** Si hay un subestado diferente a nunguno o holdUntil vigente, está bloqueado efectivamente. */
    public boolean isBlockedEffective(Pasture pasture) {
        if (pasture.getSubstatus() != null && !PastureSubstatus.NINGUNO.name().equals(pasture.getSubstatus())) {
            return true;
        }
        Instant hu = null;
        if (pasture.getHoldUntil() != null && !pasture.getHoldUntil().isBlank()) {
            try {
                LocalDate holdDate = LocalDate.parse(pasture.getHoldUntil());
                hu = holdDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (Exception ex) {
                return false;
            }
        }
        return hu != null && Instant.now().isBefore(hu);
    }

    /** Fecha disponible real considerando ETA y holdUntil. */
    public Instant availableAt(int etaOpenDays, String holdUntil) {
        Instant now = Instant.now();
        Instant readyAt = now.plus(Duration.ofDays(Math.max(etaOpenDays, 0)));
        Instant hold = null;
        if (holdUntil != null && !holdUntil.isBlank()) {
            try {
                LocalDate holdDate = LocalDate.parse(holdUntil);
                hold = holdDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (Exception e) {
                return readyAt;
            }
        }
        if (hold == null) return readyAt;
        return readyAt.isAfter(hold) ? readyAt : hold;
    }
}
