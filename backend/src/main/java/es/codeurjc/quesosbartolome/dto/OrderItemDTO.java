package es.codeurjc.quesosbartolome.dto;

import java.util.List;

public record OrderItemDTO(
    Long id,
    Long cheeseId,
    String cheeseName,
    Double cheesePrice,
    List<Double> boxes,
    Double weight,     
    Double totalPrice   
) {}