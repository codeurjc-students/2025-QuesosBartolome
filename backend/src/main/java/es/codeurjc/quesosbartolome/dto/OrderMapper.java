package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.model.Cheese;
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

    // CHEESE → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "price", target = "price")
    })
    CheeseBasicDTO toCheeseDTO(Cheese cheese);

    // DTO → CHEESE
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "price", target = "price")
    })
    Cheese toCheeseDomain(CheeseBasicDTO dto);

    // ORDER ITEM → DTO
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "cheese", target = "cheese"),
        @Mapping(source = "weight", target = "weight"),
        @Mapping(source = "price", target = "price")
    })
    OrderItemDTO toItemDTO(OrderItem item);

    // DTO → ORDER ITEM
    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "cheese", target = "cheese"),
        @Mapping(source = "weight", target = "weight"),
        @Mapping(source = "price", target = "price")
    })
    OrderItem toItemDomain(OrderItemDTO dto);
}
