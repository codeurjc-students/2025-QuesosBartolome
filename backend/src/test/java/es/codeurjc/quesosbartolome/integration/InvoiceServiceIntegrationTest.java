package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.InvoiceDTO;
import es.codeurjc.quesosbartolome.model.*;
import es.codeurjc.quesosbartolome.repository.*;
import es.codeurjc.quesosbartolome.service.InvoiceService;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InvoiceServiceIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("pepe");
        user.setOrders(new ArrayList<>());
        userRepository.save(user);
    }

    @AfterAll
    static void cleanup(
            @Autowired InvoiceRepository invoiceRepository,
            @Autowired OrderRepository orderRepository,
            @Autowired UserRepository userRepository) {

        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnAllInvoices() {
        Invoice inv = new Invoice(user, null);
        inv.setTaxableBase(10.0);
        inv.setTotalPrice(10.4);
        invoiceRepository.save(inv);

        Page<InvoiceDTO> page = invoiceService.getAllInvoices(Pageable.unpaged());

        assertThat(page).isNotEmpty();
        assertThat(page.getContent().get(0).taxableBase()).isEqualTo(10.0);
    }

    @Test
    void shouldReturnPagedInvoices() {
        for (int i = 0; i < 15; i++) {
            Invoice inv = new Invoice(user, null);
            inv.setTaxableBase(10.0 + i);
            inv.setTotalPrice(10.0 + i);
            invoiceRepository.save(inv);
        }

        Page<InvoiceDTO> page = invoiceService.getAllInvoices(Pageable.ofSize(10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldReturnInvoiceById() {
        Invoice inv = new Invoice(user, null);
        inv.setTaxableBase(20.0);
        invoiceRepository.save(inv);

        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(inv.getId());

        assertThat(result).isPresent();
        assertThat(result.get().taxableBase()).isEqualTo(20.0);
    }

    @Test
    void shouldReturnEmptyWhenInvoiceNotFound() {
        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnExistingInvoiceIfAlreadyExists() {
        Order order = new Order(user);
        orderRepository.save(order);

        Invoice inv = new Invoice(user, order);
        invoiceRepository.save(inv);

        InvoiceDTO dto = invoiceService.createFromOrder(order.getId());

        assertThat(dto.id()).isEqualTo(inv.getId());
    }

    @Test
    void createFromOrderThrowsWhenOrderNotFound() {
        assertThatThrownBy(() -> invoiceService.createFromOrder(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void createFromOrderThrowsWhenOrderAlreadyProcessed() {
        Order order = new Order(user);
        order.setProcessed(true);
        orderRepository.save(order);

        assertThatThrownBy(() -> invoiceService.createFromOrder(order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order already processed");
    }

    @Test
    void createFromOrderCreatesInvoiceSuccessfully() {
        // GIVEN
        Order order = new Order(user);
        order.setProcessed(false);
        order.setTotalPrice(30.0);

        OrderItem item1 = new OrderItem(order, 1L, "Cheese1", 10.0, List.of(1.0), 1.0, 10.0);
        OrderItem item2 = new OrderItem(order, 2L, "Cheese2", 20.0, List.of(1.0), 1.0, 20.0);

        order.getItems().add(item1);
        order.getItems().add(item2);

        orderRepository.save(order);

        // WHEN
        InvoiceDTO dto = invoiceService.createFromOrder(order.getId());

        // THEN
        Invoice saved = invoiceRepository.findById(dto.id()).orElseThrow();

        assertThat(dto.taxableBase()).isEqualTo(30.0);
        assertThat(dto.totalPrice())
        .isCloseTo((10.0 * 1.04) + (20.0 * 1.04), offset(1e-9));
        assertThat(saved.getInvNo()).startsWith("FACT-Q");
        assertThat(orderRepository.findById(order.getId()).get().isProcessed()).isTrue();
    }
}
