package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "password", target = "password"),
        @Mapping(source = "gmail", target = "gmail"),
        @Mapping(source = "direction", target = "direction"),
        @Mapping(source = "nif", target = "nif"),
        @Mapping(source = "rols", target = "rols"),
        @Mapping(source = "banned", target = "banned")
    })
    UserDTO toDTO(User user);

    // DTO -> Entity
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "password", target = "password"),
        @Mapping(source = "gmail", target = "gmail"),
        @Mapping(source = "direction", target = "direction"),
        @Mapping(source = "nif", target = "nif"),
        @Mapping(source = "rols", target = "rols"),
        @Mapping(source = "banned", target = "banned"),
        @Mapping(target = "image", ignore = true),
        @Mapping(target = "reviews", ignore = true),
        @Mapping(target = "cart", ignore = true),
        @Mapping(target = "orders", ignore = true)
    })
    User toDomain(UserDTO userDTO);
}
