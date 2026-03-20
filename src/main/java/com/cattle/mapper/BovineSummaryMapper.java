package com.cattle.mapper;

import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.entities.bovines.BovineSummary;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para conversión entre BovineSummary (Entity) y BovineSummaryDTO.
 * Utiliza MapStruct para generación automática de código.
 */
@Mapper(componentModel = "spring")
public interface BovineSummaryMapper {

    /**
     * Convierte una entidad BovineSummary a DTO.
     * @param source Entidad de origen
     * @return DTO de destino
     */
    BovineSummaryDTO toDTO(BovineSummary source);

    /**
     * Convierte un DTO a entidad BovineSummary.
     * Los campos pk, sk, gsi1pk, gsi1sk, currentLactationId, currentPregnancyId
     * se ignoran porque no están en el DTO y deben ser manejados por el servicio.
     * @param source DTO de origen
     * @return Entidad de destino
     */
    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "sk", ignore = true)
    @Mapping(target = "gsi1pk", ignore = true)
    @Mapping(target = "gsi1sk", ignore = true)
    @Mapping(target = "currentLactationId", ignore = true)
    @Mapping(target = "currentPregnancyId", ignore = true)
    BovineSummary toEntity(BovineSummaryDTO source);
}
