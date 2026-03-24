package es.codeurjc.quesosbartolome.dto;

import java.time.LocalDateTime;

public record InvoiceDTO(
        Long id,
        String invNo,
        UserBasicDTO user,
        OrderDTO order,
        Double taxableBase,
        Double totalPrice,
        LocalDateTime invoiceDate) {
}
