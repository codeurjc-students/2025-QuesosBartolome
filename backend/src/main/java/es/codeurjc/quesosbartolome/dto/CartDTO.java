package es.codeurjc.quesosbartolome.dto;

import java.util.List;

public record CartDTO(
    Long id,
    UserBasicDTO user,
    Double totalWeight,
    Double totalPrice,
    List<OrderItemDTO>items
) {}
