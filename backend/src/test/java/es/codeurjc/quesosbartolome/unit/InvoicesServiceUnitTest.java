package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.InvoiceDTO;
import es.codeurjc.quesosbartolome.dto.InvoiceMapper;
import es.codeurjc.quesosbartolome.dto.OrderMapper;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.InvoiceRepository;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.service.InvoiceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private InvoiceMapper invoiceMapper = Mappers.getMapper(InvoiceMapper.class);

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    void setUpMapperDependencies() {
        ReflectionTestUtils.setField(invoiceMapper, "orderMapper", Mappers.getMapper(OrderMapper.class));
    }

    @Test
    void getAllInvoicesReturnsMappedPage() {
        Invoice i1 = new Invoice();
        i1.setId(1L);
        Invoice i2 = new Invoice();
        i2.setId(2L);

        Page<Invoice> page = new PageImpl<>(List.of(i1, i2));
        when(invoiceRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<InvoiceDTO> result = invoiceService.getAllInvoices(Pageable.unpaged());

        assertThat(result).hasSize(2);
        assertThat(result.map(InvoiceDTO::id).toList()).contains(1L, 2L);
        verify(invoiceRepository).findAll(any(Pageable.class));
    }

    @Test
    void getInvoiceByIdReturnsDTOWhenFound() {
        Invoice invoice = new Invoice();
        invoice.setId(10L);

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));

        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(10L);
    }

    @Test
    void getInvoiceByIdReturnsEmptyWhenNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createFromOrderReturnsExistingInvoiceIfPresent() {
        Invoice invoice = new Invoice();
        invoice.setId(5L);

        when(invoiceRepository.findByOrderId(1L)).thenReturn(Optional.of(invoice));

        InvoiceDTO dto = invoiceService.createFromOrder(1L);

        assertThat(dto.id()).isEqualTo(5L);
        verify(invoiceRepository, never()).save(any());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void createFromOrderThrowsWhenOrderNotFound() {
        when(invoiceRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.createFromOrder(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void createFromOrderThrowsWhenOrderAlreadyProcessed() {
        Order order = new Order();
        order.setProcessed(true);

        when(invoiceRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> invoiceService.createFromOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order already processed");
    }

    @Test
    void createFromOrderCreatesInvoiceSuccessfully() {
        // GIVEN
        User user = new User();
        user.setId(1L);

        OrderItem item1 = new OrderItem();
        item1.setTotalPrice(10.0);

        OrderItem item2 = new OrderItem();
        item2.setTotalPrice(20.0);

        Order order = new Order(user);
        order.setId(1L);
        order.setProcessed(false);
        order.setTotalPrice(30.0);
        order.getItems().addAll(List.of(item1, item2));

        when(invoiceRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice invc = inv.getArgument(0);
            invc.setId(50L);
            invc.setInvoiceDate(LocalDateTime.of(2024, 1, 1, 0, 0));
            return invc;
        });

        // WHEN
        InvoiceDTO dto = invoiceService.createFromOrder(1L);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(50L);
        assertThat(dto.taxableBase()).isEqualTo(30.0);
        assertThat(dto.totalPrice()).isCloseTo((10.0 * 1.04) + (20.0 * 1.04), within(0.000001));

        assertThat(order.isProcessed()).isTrue();

        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(orderRepository).save(order);
    }
}
