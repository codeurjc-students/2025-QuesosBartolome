package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {OrderMapper.class})
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
        @Mapping(source = "currentOrder", target = "currentOrder")
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
        @Mapping(source = "currentOrder", target = "currentOrder")
    })
    User toDomain(UserDTO userDTO);
}
