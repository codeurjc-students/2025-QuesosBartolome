package es.codeurjc.quesosbartolome.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
    Long id,
    UserBasicDTO user,
    Double totalWeight,
    Double totalPrice,
    LocalDateTime orderDate,
    List<OrderItemDTO> items
) {}