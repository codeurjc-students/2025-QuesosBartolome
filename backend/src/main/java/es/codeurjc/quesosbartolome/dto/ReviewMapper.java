package es.codeurjc.quesosbartolome.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import es.codeurjc.quesosbartolome.model.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // Entity -> DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "rating", target = "rating"),
        @Mapping(source = "comment", target = "comment"),
        @Mapping(source = "user.id", target = "user.id"),
        @Mapping(source = "user.name", target = "user.name"),
        @Mapping(source = "cheese.id", target = "cheese.id"),
        @Mapping(source = "cheese.name", target = "cheese.name"),
        @Mapping(source = "cheese.price", target = "cheese.price")
    })
    ReviewDTO toDTO(Review review);
}
