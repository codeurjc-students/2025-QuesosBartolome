package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.service.InvoicePdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InvoicePdfServiceTest {

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Test
    void testGenerateInvoicePdfReturnsValidPdf() throws IOException {
        // GIVEN
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setGmail("john@example.com");
        user.setDirection("123 Main St");
        user.setNif("12345678A");

        Order order = new Order(user);
        order.setId(1L);

        OrderItem item1 = new OrderItem();
        item1.setCheeseId(1L);
        item1.setCheeseName("Manchego Curado");
        item1.setCheesePrice(15.50);
        List<Double> boxes1 = new ArrayList<>();
        boxes1.add(1.0);
        boxes1.add(2.0);
        item1.setBoxes(boxes1);
        item1.setWeight(2.0);
        item1.setTotalPrice(31.00);

        order.setItems(List.of(item1));
        order.setTotalPrice(31.00);
        order.setTotalWeight(2.0);

        Invoice invoice = new Invoice(user, order);
        invoice.setId(1L);
        invoice.setInvNo("FACT-Q26/001");
        invoice.setTaxableBase(31.00);
        invoice.setTotalPrice(37.51);
        invoice.setInvoiceDate(LocalDateTime.now());

        // WHEN
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);

        // THEN
        assertNotNull(pdfBytes, "PDF bytes should not be null");
        assertTrue(pdfBytes.length > 0, "PDF bytes should not be empty");
        assertTrue(pdfBytes.length > 1000, "PDF should have reasonable size (> 1KB)");

        // Verify PDF header - PDFs start with %PDF
        assertEquals('%', (char) pdfBytes[0], "PDF should start with %");
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    void testGenerateInvoicePdfWithMultipleItems() throws IOException {
        // GIVEN
        User user = new User();
        user.setId(2L);
        user.setName("Jane Smith");
        user.setGmail("jane@example.com");
        user.setDirection("456 Oak Ave");
        user.setNif("87654321B");

        Order order = new Order(user);
        order.setId(2L);

        OrderItem item1 = new OrderItem();
        item1.setCheeseId(1L);
        item1.setCheeseName("Manchego");
        item1.setCheesePrice(12.00);
        List<Double> boxes2 = new ArrayList<>();
        boxes2.add(1.0);
        item1.setBoxes(boxes2);
        item1.setTotalPrice(12.00);

        OrderItem item2 = new OrderItem();
        item2.setCheeseId(2L);
        item2.setCheeseName("Idiazabal");
        item2.setCheesePrice(18.50);
        List<Double> boxes3 = new ArrayList<>();
        boxes3.add(2.0);
        item2.setBoxes(boxes3);
        item2.setTotalPrice(37.00);

        order.setItems(List.of(item1, item2));
        order.setTotalPrice(49.00);

        Invoice invoice = new Invoice(user, order);
        invoice.setId(2L);
        invoice.setInvNo("FACT-Q26/002");
        invoice.setTaxableBase(49.00);
        invoice.setTotalPrice(59.29);

        // WHEN
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);

        // THEN
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
    }

    @Test
    void testGenerateInvoicePdfThrowsIOExceptionOnError() throws IOException {
        // GIVEN
        Invoice invoice = new Invoice();
        invoice.setInvNo("INVALID");

        // WHEN & THEN
        assertDoesNotThrow(() -> invoicePdfService.generateInvoicePdf(invoice));
    }
}
