package es.codeurjc.quesosbartolome.dto;

import es.codeurjc.quesosbartolome.model.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = { OrderMapper.class })
public interface InvoiceMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "invNo", target = "invNo"),
            @Mapping(source = "user", target = "user"),
            @Mapping(source = "order", target = "order"),
            @Mapping(source = "taxableBase", target = "taxableBase"),
            @Mapping(source = "totalPrice", target = "totalPrice"),
            @Mapping(source = "invoiceDate", target = "invoiceDate")
    })
    InvoiceDTO toDTO(Invoice invoice);
}
