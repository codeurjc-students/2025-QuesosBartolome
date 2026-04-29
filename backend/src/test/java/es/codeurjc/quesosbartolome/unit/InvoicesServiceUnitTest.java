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
import es.codeurjc.quesosbartolome.repository.UserRepository;
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

    @Mock
    private UserRepository userRepository;

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
    void getInvoicesForUserReturnsMappedPageWhenUserExists() {
        User user = new User();
        user.setId(8L);
        user.setName("maria");

        Invoice i1 = new Invoice();
        i1.setId(30L);
        Invoice i2 = new Invoice();
        i2.setId(31L);

        Page<Invoice> page = new PageImpl<>(List.of(i1, i2));

        when(userRepository.findByName("maria")).thenReturn(Optional.of(user));
        when(invoiceRepository.findByUserIdOrderByInvoiceDateDesc(8L, Pageable.unpaged())).thenReturn(page);

        Page<InvoiceDTO> result = invoiceService.getInvoicesForUser("maria", Pageable.unpaged());

        assertThat(result).hasSize(2);
        assertThat(result.map(InvoiceDTO::id).toList()).containsExactly(30L, 31L);
        verify(userRepository).findByName("maria");
        verify(invoiceRepository).findByUserIdOrderByInvoiceDateDesc(8L, Pageable.unpaged());
    }

    @Test
    void getInvoicesForUserThrowsWhenUserNotFound() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoicesForUser("ghost", Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByName("ghost");
        verify(invoiceRepository, never()).findByUserIdOrderByInvoiceDateDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getInvoiceByIdForUserReturnsEmptyWhenUserNotFound() {
        when(userRepository.findByName("unknown")).thenReturn(Optional.empty());

        Optional<InvoiceDTO> result = invoiceService.getInvoiceByIdForUser(2L, "unknown");

        assertThat(result).isEmpty();
        verify(userRepository).findByName("unknown");
        verify(invoiceRepository, never()).findByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void getInvoiceByIdForUserReturnsDTOWhenFound() {
        User user = new User();
        user.setId(14L);
        user.setName("pepe");

        Invoice invoice = new Invoice();
        invoice.setId(77L);

        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(invoiceRepository.findByIdAndUserId(77L, 14L)).thenReturn(Optional.of(invoice));

        Optional<InvoiceDTO> result = invoiceService.getInvoiceByIdForUser(77L, "pepe");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(77L);
    }

    @Test
    void getInvoiceByIdForUserReturnsEmptyWhenInvoiceNotFoundForUser() {
        User user = new User();
        user.setId(15L);
        user.setName("ana");

        when(userRepository.findByName("ana")).thenReturn(Optional.of(user));
        when(invoiceRepository.findByIdAndUserId(101L, 15L)).thenReturn(Optional.empty());

        Optional<InvoiceDTO> result = invoiceService.getInvoiceByIdForUser(101L, "ana");

        assertThat(result).isEmpty();
    }

    @Test
    void getInvoiceEntityReturnsEntityWhenFound() {
        Invoice invoice = new Invoice();
        invoice.setId(200L);

        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(invoice));

        Optional<Invoice> result = invoiceService.getInvoiceEntity(200L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(200L);
    }

    @Test
    void getInvoiceEntityForUserReturnsEmptyWhenUserNotFound() {
        when(userRepository.findByName("unknown")).thenReturn(Optional.empty());

        Optional<Invoice> result = invoiceService.getInvoiceEntityForUser(5L, "unknown");

        assertThat(result).isEmpty();
        verify(invoiceRepository, never()).findByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void getInvoiceEntityForUserReturnsEntityWhenFound() {
        User user = new User();
        user.setId(17L);
        user.setName("raul");

        Invoice invoice = new Invoice();
        invoice.setId(300L);

        when(userRepository.findByName("raul")).thenReturn(Optional.of(user));
        when(invoiceRepository.findByIdAndUserId(300L, 17L)).thenReturn(Optional.of(invoice));

        Optional<Invoice> result = invoiceService.getInvoiceEntityForUser(300L, "raul");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(300L);
    }

    @Test
    void existsByOrderIdReturnsTrueWhenInvoiceExists() {
        when(invoiceRepository.findByOrderId(9L)).thenReturn(Optional.of(new Invoice()));

        boolean result = invoiceService.existsByOrderId(9L);

        assertThat(result).isTrue();
    }

    @Test
    void existsByOrderIdReturnsFalseWhenInvoiceDoesNotExist() {
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.empty());

        boolean result = invoiceService.existsByOrderId(10L);

        assertThat(result).isFalse();
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
