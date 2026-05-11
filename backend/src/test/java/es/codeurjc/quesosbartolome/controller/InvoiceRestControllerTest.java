package es.codeurjc.quesosbartolome.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.service.InvoicePdfService;
import es.codeurjc.quesosbartolome.service.InvoiceService;
import es.codeurjc.quesosbartolome.service.UserService;

public class InvoiceRestControllerTest {

    @Test
    void downloadInvoicePdf_whenPdfGenerationThrowsIOException_returnsInternalServerError() throws Exception {
        InvoiceRestController controller = new InvoiceRestController();

        InvoiceService invoiceService = mock(InvoiceService.class);
        InvoicePdfService invoicePdfService = mock(InvoicePdfService.class);
        UserService userService = mock(UserService.class);

        ReflectionTestUtils.setField(controller, "invoiceService", invoiceService);
        ReflectionTestUtils.setField(controller, "invoicePdfService", invoicePdfService);
        ReflectionTestUtils.setField(controller, "userService", userService);

        Invoice invoice = new Invoice();
        invoice.setInvNo("INV-1");

        when(invoiceService.getInvoiceEntity(1L)).thenReturn(Optional.of(invoice));
        when(invoicePdfService.generateInvoicePdf(invoice)).thenThrow(new IOException("simulated IO"));

        ResponseEntity<byte[]> response = controller.downloadInvoicePdf(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

}
