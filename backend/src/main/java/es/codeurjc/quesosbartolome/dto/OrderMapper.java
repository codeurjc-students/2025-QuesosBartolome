package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ORDER → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "user", target = "user"),
        @Mapping(source = "totalWeight", target = "totalWeight"),
        @Mapping(source = "totalPrice", target = "totalPrice"),
        @Mapping(source = "orderDate", target = "orderDate"),
        @Mapping(source = "items", target = "items")
    })
    OrderDTO toDTO(Order order);

    // DTO → ORDER
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "user", target = "user"),
        @Mapping(source = "totalWeight", target = "totalWeight"),
        @Mapping(source = "totalPrice", target = "totalPrice"),
        @Mapping(source = "orderDate", target = "orderDate"),
        @Mapping(source = "items", target = "items")
    })
    Order toDomain(OrderDTO dto);

    // USER → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name")
    })
    UserBasicDTO toUserDTO(User user);

    // DTO → USER
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name")
    })
    User toUserDomain(UserBasicDTO dto);

    // ORDER ITEM → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "cheeseName", target = "cheeseName"),
        @Mapping(source = "cheesePrice", target = "cheesePrice"),
        @Mapping(source = "boxes", target = "boxes"),
        @Mapping(source = "weight", target = "weight"),
        @Mapping(source = "totalPrice", target = "totalPrice")
    })
    OrderItemDTO toItemDTO(OrderItem item);

    // DTO → ORDER ITEM
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "cheeseName", target = "cheeseName"),
        @Mapping(source = "cheesePrice", target = "cheesePrice"),
        @Mapping(source = "boxes", target = "boxes"),
        @Mapping(source = "weight", target = "weight"),
        @Mapping(source = "totalPrice", target = "totalPrice")
    })
    OrderItem toItemDomain(OrderItemDTO dto);
}
