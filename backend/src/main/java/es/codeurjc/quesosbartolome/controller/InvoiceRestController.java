package es.codeurjc.quesosbartolome.controller;

import es.codeurjc.quesosbartolome.dto.InvoiceDTO;
import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.service.InvoiceService;
import es.codeurjc.quesosbartolome.service.InvoicePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceRestController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoicePdfService invoicePdfService;

    @GetMapping
    public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody OrderDTO dto) {
        if (dto == null || dto.id() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean alreadyExisted = invoiceService.existsByOrderId(dto.id());
            InvoiceDTO created = invoiceService.createFromOrder(dto.id());
            if (alreadyExisted) {
                return ResponseEntity.ok(created);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{id}/download-pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.getInvoiceEntity(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

            byte[] pdfContent = invoicePdfService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfContent.length);
            headers.setContentDispositionFormData("attachment", invoice.getInvNo() + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
