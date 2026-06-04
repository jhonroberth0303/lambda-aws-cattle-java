package com.cattle.mapper;

import com.cattle.dtos.MilkingDTO;
import com.cattle.entities.MilkingRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MilkingMapper {

    MilkingDTO toDTO(MilkingRecord source);

    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "sk", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "gsi1pk", ignore = true)
    @Mapping(target = "gsi1sk", ignore = true)
    MilkingRecord toEntity(MilkingDTO source);
}
