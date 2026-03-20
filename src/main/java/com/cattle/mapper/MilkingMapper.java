package com.cattle.mapper;

import com.cattle.dtos.MilkingDTO;
import com.cattle.entities.MilkingRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MilkingMapper {

    MilkingDTO toDTO(MilkingRecord source);

    @Mapping(target = "PK", ignore = true)
    @Mapping(target = "SK", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "gsi2pk", ignore = true)
    @Mapping(target = "gsi2sk", ignore = true)
    MilkingRecord toEntity(MilkingDTO source);
}
