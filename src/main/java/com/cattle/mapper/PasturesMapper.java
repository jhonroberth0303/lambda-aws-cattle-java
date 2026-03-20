package com.cattle.mapper;

import com.cattle.entities.Pasture;
import com.cattle.dtos.PastureDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PasturesMapper {

    PastureDTO toDTO(Pasture entity);
    Pasture toEntity(PastureDTO dto);
    List<PastureDTO> toDTOList(List<Pasture> entities);
    List<Pasture> toEntityList(List<PastureDTO> dtos);
}

