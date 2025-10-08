package es.codeurjc.quesosbartolome.dto;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import es.codeurjc.quesosbartolome.model.Cheese;

@Mapper(componentModel = "spring")
public interface CheeseMapper {

    // Entity -> DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "price", target = "price"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "manufactureDate", target = "manufactureDate"),
        @Mapping(source = "expirationDate", target = "expirationDate"),
        @Mapping(source = "type", target = "Type"),
        @Mapping(source = "image", target = "image")
    })
    CheeseDTO toDTO(Cheese cheese);

    // DTO -> Entity
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "price", target = "price"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "manufactureDate", target = "manufactureDate"),
        @Mapping(source = "expirationDate", target = "expirationDate"),
        @Mapping(source = "Type", target = "type"),
        @Mapping(source = "image", target = "image")
    })
    Cheese toDomain(CheeseDTO cheeseDTO);
}
