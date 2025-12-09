package es.codeurjc.quesosbartolome.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import es.codeurjc.quesosbartolome.model.User;

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
        @Mapping(source = "rols", target = "rols")
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
        @Mapping(source = "rols", target = "rols")
    })
    User toDomain(UserDTO userDTO);
    
}
