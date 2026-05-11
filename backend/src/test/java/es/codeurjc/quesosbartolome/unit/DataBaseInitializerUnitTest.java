package es.codeurjc.quesosbartolome.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.Review;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.InvoiceRepository;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.ReviewRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.DataBaseInitializer;

@ExtendWith(MockitoExtension.class)
class DataBaseInitializerUnitTest {

    @Mock
    private CheeseRepository cheeseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataBaseInitializer dataBaseInitializer;

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = DataBaseInitializer.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(dataBaseInitializer, args);
    }

    @Test
    void saveImageReturnsBlobWhenResourceExists() throws IOException {
        var blob = dataBaseInitializer.saveImage("images/default-profile.jpg");

        assertThat(blob).isNotNull();
    }

    @Test
    void saveImageThrowsWhenResourceDoesNotExist() {
        assertThatThrownBy(() -> dataBaseInitializer.saveImage("images/does-not-exist.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File not found in classpath");
    }

    @Test
    void initRunsTestBranchAndSeedsBasicDataWithoutFailing() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(cheeseRepository.save(any(Cheese.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataBaseInitializer.init();

        verify(reviewRepository, atLeastOnce()).save(any(Review.class));
        verify(cheeseRepository, atLeastOnce()).save(any(Cheese.class));
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(passwordEncoder, atLeastOnce()).encode("password123");
    }

    @Test
    void pickBoxesUsesExpectedInventoryByCheeseName() throws Exception {
        Cheese azul = new Cheese();
        azul.setName("Azul");

        Cheese semicurado = new Cheese();
        semicurado.setName("Semicurado");

        List<Double> azulBoxes = invokePrivate("pickBoxes",
                new Class<?>[] { Cheese.class, int.class, int.class },
                azul, 3, 0);
        List<Double> semicuradoBoxes = invokePrivate("pickBoxes",
                new Class<?>[] { Cheese.class, int.class, int.class },
                semicurado, 3, 0);

        assertThat(azulBoxes).containsExactly(4.05, 4.28, 4.50);
        assertThat(semicuradoBoxes).containsExactly(5.12, 5.55, 5.89);
    }

    @Test
    void round2RoundsAsExpected() throws Exception {
        Double roundedUp = invokePrivate("round2", new Class<?>[] { double.class }, 12.345d);
        Double roundedDown = invokePrivate("round2", new Class<?>[] { double.class }, 12.344d);

        assertThat(roundedUp).isEqualTo(12.35d);
        assertThat(roundedDown).isEqualTo(12.34d);
    }

    @Test
    void isRunningTestReturnsTrueWhenJUnitIsAvailable() throws Exception {
        Boolean runningTest = invokePrivate("isRunningTest", new Class<?>[] {});

        assertThat(runningTest).isTrue();
    }

    @Test
    void createMixedOrderBuildsItemsTotalsAndPersistsOrder() throws Exception {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        user.setName("test-user");

        Cheese semicurado = new Cheese();
        semicurado.setId(1L);
        semicurado.setName("Semicurado");
        semicurado.setPrice(17.50);

        Cheese azul = new Cheese();
        azul.setId(2L);
        azul.setName("Azul");
        azul.setPrice(15.00);

        LocalDateTime orderDate = LocalDateTime.of(2026, 2, 10, 10, 30);

        Order order = invokePrivate("createMixedOrder",
                new Class<?>[] { User.class, LocalDateTime.class, boolean.class, int.class, Cheese[].class,
                        int[].class },
                user, orderDate, true, 7, new Cheese[] { semicurado, azul }, new int[] { 2, 1 });

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getOrderDate()).isEqualTo(orderDate);
        assertThat(order.isProcessed()).isTrue();
        assertThat(order.getTotalWeight()).isGreaterThan(0.0);
        assertThat(order.getTotalPrice()).isGreaterThan(0.0);
        assertThat(user.getOrders()).contains(order);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void createInvoiceFormatsInvoiceNumberAndPersistsTwice() throws Exception {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        Order order = new Order(user);
        LocalDateTime invoiceDate = LocalDateTime.of(2026, 3, 13, 9, 45);

        invokePrivate("createInvoice",
                new Class<?>[] { User.class, Order.class, double.class, double.class, LocalDateTime.class, int.class },
                user, order, 123.456d, 128.391d, invoiceDate, 9003);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());

        List<Invoice> savedInvoices = invoiceCaptor.getAllValues();
        Invoice firstSave = savedInvoices.get(0);
        Invoice secondSave = savedInvoices.get(1);

        assertThat(firstSave.getTaxableBase()).isEqualTo(123.46d);
        assertThat(firstSave.getTotalPrice()).isEqualTo(128.39d);
        assertThat(firstSave.getInvoiceDate()).isEqualTo(invoiceDate);
        assertThat(secondSave.getInvNo()).isEqualTo("FACT-Q26/9003");
    }
}