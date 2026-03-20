package com.cattle.mapper;


import com.cattle.dtos.BovineDTO;
import com.cattle.dtos.BreedCompositionDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.BreedComposition;
import com.cattle.enums.profiles.BovineOrigin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BovinesMapper {


    @Mapping(target = "origin", source = "origin", qualifiedByName = "stringToOrigin")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "stringToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "stringToInstant")
    @Mapping(target = "breedComposition", source = "breedComposition", qualifiedByName = "dtoListToEntityList")
    BovineDTO toDTO(BovineIdentityItem source);

    @Mapping(target = "origin", source = "origin", qualifiedByName = "originToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToString")
    @Mapping(target = "breedComposition", source = "breedComposition", qualifiedByName = "entityListToDtoList")
    BovineIdentityItem toEntity(BovineDTO source);
    @Named("dtoListToEntityList")
    static java.util.List<BreedCompositionDTO> dtoListToEntityList(java.util.List<BreedComposition> list) {
        if (list == null) return null;
        return list.stream()
            .map(e -> new BreedCompositionDTO(e.getBreed(), e.getPct()))
            .toList();
    }

    @Named("entityListToDtoList")
    static java.util.List<BreedComposition> entityListToDtoList(java.util.List<BreedCompositionDTO> list) {
        if (list == null) return null;
        return list.stream()
            .map(d -> new BreedComposition(d.getBreed(), d.getPct()))
            .toList();
    }

    @Named("stringToInstant")
    static java.time.Instant stringToInstant(String value) {
        if (value == null) return null;
        try {
            return java.time.Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("instantToString")
    static String instantToString(java.time.Instant value) {
        return value != null ? value.toString() : null;
    }

    @Named("stringToOrigin")
    static BovineOrigin stringToOrigin(String origin) {
        if (origin == null) return null;
        try {
            return BovineOrigin.valueOf(origin.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    @Named("originToString")
    static String originToString(BovineOrigin origin) {
        return origin != null ? origin.name() : null;
    }
}
