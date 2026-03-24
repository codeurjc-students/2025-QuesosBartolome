package es.codeurjc.quesosbartolome.service;

import com.itextpdf.html2pdf.HtmlConverter;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class InvoicePdfService {

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Generates a PDF from an invoice using Thymeleaf and iText7
     * @param invoice The invoice to generate PDF from
     * @return PDF as byte array
     * @throws IOException if PDF generation fails
     */
    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        Invoice safeInvoice = normalizeInvoice(invoice);

        // Prepare Thymeleaf context with invoice data
        Context context = new Context();
        context.setVariable("invoice", safeInvoice);

        // Render HTML from template
        String htmlContent = templateEngine.process("invoice", context);

        // Convert HTML to PDF
        return htmlToPdf(htmlContent);
    }

    private Invoice normalizeInvoice(Invoice source) {
        Invoice invoice = source != null ? source : new Invoice();

        if (invoice.getUser() == null) {
            User fallbackUser = new User();
            fallbackUser.setName("Cliente");
            fallbackUser.setGmail("-");
            fallbackUser.setDirection("-");
            fallbackUser.setNif("-");
            invoice.setUser(fallbackUser);
        }

        if (invoice.getOrder() == null) {
            invoice.setOrder(new Order());
        }

        if (invoice.getOrder().getItems() == null) {
            invoice.getOrder().setItems(new ArrayList<>());
        }

        if (invoice.getTaxableBase() == null) {
            invoice.setTaxableBase(0.0);
        }

        if (invoice.getTotalPrice() == null) {
            invoice.setTotalPrice(0.0);
        }

        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDateTime.now());
        }

        if (invoice.getInvNo() == null || invoice.getInvNo().isBlank()) {
            invoice.setInvNo("SIN-REFERENCIA");
        }

        return invoice;
    }

    /**
     * Converts HTML string to PDF bytes using iText7
     * @param htmlContent HTML content as string
     * @return PDF as byte array
     * @throws IOException if conversion fails
     */
    private byte[] htmlToPdf(String htmlContent) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
        } catch (Exception e) {
            throw new IOException("Error generating PDF from HTML", e);
        }

        return outputStream.toByteArray();
    }
}
