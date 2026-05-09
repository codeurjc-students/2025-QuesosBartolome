package es.codeurjc.quesosbartolome.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}