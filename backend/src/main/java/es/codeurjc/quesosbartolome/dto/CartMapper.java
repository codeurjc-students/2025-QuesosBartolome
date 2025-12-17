package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.Cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // CART → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "user", target = "user"),
        @Mapping(source = "totalWeight", target = "totalWeight"),
        @Mapping(source = "totalPrice", target = "totalPrice"),
        @Mapping(source = "items", target = "items")
    })
    CartDTO toDTO(Cart cart);

    // DTO → CART
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "user", target = "user"),
        @Mapping(source = "totalWeight", target = "totalWeight"),
        @Mapping(source = "totalPrice", target = "totalPrice"),
        @Mapping(source = "items", target = "items")
    })
    Cart toDomain(CartDTO dto);

}