package es.codeurjc.quesosbartolome.service;

import es.codeurjc.quesosbartolome.dto.InvoiceDTO;
import es.codeurjc.quesosbartolome.dto.InvoiceMapper;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.repository.InvoiceRepository;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceMapper invoiceMapper;

    public Page<InvoiceDTO> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable)
                .map(invoiceMapper::toDTO);
    }

    public Page<InvoiceDTO> getInvoicesForUser(String username, Pageable pageable) {
        Long userId = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        return invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId, pageable)
                .map(invoiceMapper::toDTO);
    }

    public Optional<InvoiceDTO> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(invoiceMapper::toDTO);
    }

    public Optional<InvoiceDTO> getInvoiceByIdForUser(Long invoiceId, String username) {
        Optional<Long> userId = userRepository.findByName(username).map(user -> user.getId());
        if (userId.isEmpty()) {
            return Optional.empty();
        }

        return invoiceRepository.findByIdAndUserId(invoiceId, userId.get())
                .map(invoiceMapper::toDTO);
    }

    public Optional<Invoice> getInvoiceEntity(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    public Optional<Invoice> getInvoiceEntityForUser(Long invoiceId, String username) {
        Optional<Long> userId = userRepository.findByName(username).map(user -> user.getId());
        if (userId.isEmpty()) {
            return Optional.empty();
        }

        return invoiceRepository.findByIdAndUserId(invoiceId, userId.get());
    }

    public boolean existsByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).isPresent();
    }

    public InvoiceDTO createFromOrder(Long orderId) {
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrderId(orderId);
        if (existingInvoice.isPresent()) {
            return invoiceMapper.toDTO(existingInvoice.get());
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.isProcessed()) {
            throw new IllegalStateException("Order already processed");
        }

        double taxableBase = order.getTotalPrice() != null ? order.getTotalPrice() : 0.0;
        double totalWithIva = order.getItems().stream()
                .mapToDouble(item -> (item.getTotalPrice() != null ? item.getTotalPrice() : 0.0) * 1.04)
                .sum();

        Invoice invoice = new Invoice(order.getUser(), order);
        invoice.setTaxableBase(taxableBase);
        invoice.setTotalPrice(totalWithIva);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        int yearSuffix = savedInvoice.getInvoiceDate().getYear() % 100;
        savedInvoice.setInvNo(String.format("FACT-Q%02d/%d", yearSuffix, savedInvoice.getId()));
        savedInvoice = invoiceRepository.save(savedInvoice);

        order.setProcessed(true);
        orderRepository.save(order);

        return invoiceMapper.toDTO(savedInvoice);
    }
}
