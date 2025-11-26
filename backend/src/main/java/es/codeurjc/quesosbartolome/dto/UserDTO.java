package es.codeurjc.quesosbartolome.dto;




public record UserDTO(
    Long id,
    String name,
    String password,
    String gmail,
    String direction,
    String nif
) {}