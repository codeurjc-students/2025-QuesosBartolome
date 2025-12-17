package es.codeurjc.quesosbartolome.dto;

public record OrderItemDTO(
    Long id,
    CheeseBasicDTO cheese,
    Double weight,     
    Double price   
) {}