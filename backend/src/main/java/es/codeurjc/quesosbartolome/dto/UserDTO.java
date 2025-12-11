package es.codeurjc.quesosbartolome.dto;

import java.util.List;

public record UserDTO(
    Long id,
    String name,
    String password,
    String gmail,
    String direction,
    String nif,
    List<String> rols,
    OrderDTO currentOrder
) {}