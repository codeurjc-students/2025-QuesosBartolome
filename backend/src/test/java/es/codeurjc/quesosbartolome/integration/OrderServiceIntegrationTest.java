package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CartRepository;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.OrderService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CheeseRepository cheeseRepository;

    private User user;
    private Cheese cheese;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        cartRepository.deleteAll();
        cheeseRepository.deleteAll();

        // Usuario con carrito
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        cart.setTotalWeight(0.0);
        cartRepository.save(cart);

        user = new User();
        user.setName("pepe");
        user.setCart(cart);
        user.setOrders(new ArrayList<>());
        userRepository.save(user);

        // Queso con cajas
        cheese = new Cheese(null, "Curado", 10.0, "desc", "tipo", "2024-01-01", "2025-01-01");
        cheese.setBoxes(new ArrayList<>(List.of(1.0, 2.0, 3.0)));
        cheeseRepository.save(cheese);
    }

    @AfterAll
    static void afterAll(@Autowired OrderRepository orderRepository,
            @Autowired UserRepository userRepository,
            @Autowired CartRepository cartRepository,
            @Autowired CheeseRepository cheeseRepository) {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        cartRepository.deleteAll();
        cheeseRepository.deleteAll();
    }

    @Test
    void shouldReturnAllOrders() {
        Order order = new Order(user);
        order.setTotalPrice(10.0);
        order.setTotalWeight(1.0);
        orderRepository.save(order);

        Page<OrderDTO> page = orderService.getAllOrders(Pageable.unpaged());
        assertThat(page).isNotEmpty();
        assertThat(page.getContent().get(0).totalPrice()).isEqualTo(10.0);
    }

    @Test
    void shouldReturnPagedOrders() {
        // Crear 15 pedidos
        for (int i = 0; i < 15; i++) {
            Order order = new Order(user);
            order.setTotalPrice(10.0 + i);
            order.setTotalWeight(1.0);
            orderRepository.save(order);
        }

        Pageable pageable = Pageable.ofSize(10).withPage(0);

        Page<OrderDTO> page = orderService.getAllOrders(pageable);

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldFindOwnerByName() {
        Optional<User> found = orderService.findOwnerByName("pepe");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pepe");
    }

    @Test
    void confirmOrderThrowsWhenUserNotFound() {
        assertThatThrownBy(() -> orderService.confirmOrder(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void confirmOrderThrowsWhenCartEmpty() {
        assertThatThrownBy(() -> orderService.confirmOrder(user.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void confirmOrderCreatesOrderSuccessfully() {
        // Añadimos item al carrito
        OrderItem cartItem = new OrderItem();
        cartItem.setCheese(cheese);
        cartItem.setWeight(2.0);
        cartItem.setPrice(20.0);
        user.getCart().getItems().add(cartItem);
        user.getCart().setTotalWeight(2.0);
        user.getCart().setTotalPrice(20.0);
        userRepository.save(user);

        // Confirmamos pedido
        OrderDTO dto = orderService.confirmOrder(user.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.totalPrice()).isEqualTo(20.0);
        assertThat(user.getOrders()).hasSize(1);
        assertThat(user.getCart().getItems()).isEmpty();
        assertThat(user.getCart().getTotalPrice()).isEqualTo(0.0);
        assertThat(user.getCart().getTotalWeight()).isEqualTo(0.0);
    }
}
