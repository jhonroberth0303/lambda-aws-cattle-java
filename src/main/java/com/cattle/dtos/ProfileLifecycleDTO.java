package com.cattle.dtos;

import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.enums.profiles.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Perfil de ciclo de vida del bovino. Define su estado administrativo y categoría general.")
public class ProfileLifecycleDTO {

    private String PK;
    private String SK;

    @Schema(description = "Etapa de vida basada en edad (100% derivable)")
    private LifeStage lifeStage;
    @Schema(description = "Fuente del lifeStage: AUTO (batch) o MANUAL")
    private Source lifeStageSource;

    @Schema(description = "Categoría zootécnica del bovino")
    private BovineCategory category;
    @Schema(description = "Estado del bovino en el sistema, Muerto, Vendido , open, etc.")
    private LifecycleStatus status;
    @Schema(description = "Fuente de la categoría: AUTO (batch) o MANUAL (decisión humana)")
    private Source categorySource;

    @Schema(description = "Indica si el bovino está habilitado para operaciones en el sistema")
    private Boolean enabled;
    private String notes;

    @Schema(description = "Fecha de última evaluación por batch")
    private Instant lastEvaluatedAt;
    @Schema(description = "Próxima fecha de recalculación programada")
    private String nextRecalcDate;

    private Instant updatedAt;
    private String GSI1PK;
    private String GSI1SK;

    @Override
    public String toString() {
        return "ProfileLifecycleDTO{" +
                "PK='" + PK + '\'' +
                ", SK='" + SK + '\'' +
                ", lifeStage=" + lifeStage +
                ", lifeStageSource=" + lifeStageSource +
                ", category=" + category +
                ", status=" + status +
                ", categorySource=" + categorySource +
                ", enabled=" + enabled +
                ", notes='" + notes + '\'' +
                ", lastEvaluatedAt=" + lastEvaluatedAt +
                ", nextRecalcDate='" + nextRecalcDate + '\'' +
                ", updatedAt=" + updatedAt +
                ", GSI1PK='" + GSI1PK + '\'' +
                ", GSI1SK='" + GSI1SK + '\'' +
                '}';
    }
}
