package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.CartDTO;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CartRepository;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.CartService;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheeseRepository cheeseRepository;

    @Autowired
    private CartRepository cartRepository;

    private User user;
    private Cheese cheese;

    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        userRepository.deleteAll();
        cheeseRepository.deleteAll();

        // Creamos usuario con carrito vacío
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        cart.setTotalWeight(0.0);
        cartRepository.save(cart);

        user = new User();
        user.setName("pepe");
        user.setCart(cart);
        userRepository.save(user);

        // Creamos un queso con cajas
        cheese = new Cheese(null, "Curado", 10.0, "desc", "tipo", "2024-01-01", "2025-01-01");
        cheese.setBoxes(new ArrayList<>(List.of(1.0, 2.0, 3.0)));
        cheeseRepository.save(cheese);
    }

    @AfterAll
    static void afterAll(@Autowired UserRepository userRepository,
                         @Autowired CartRepository cartRepository,
                         @Autowired CheeseRepository cheeseRepository) {
        userRepository.deleteAll();
        cartRepository.deleteAll();
        cheeseRepository.deleteAll();
    }

    @Test
    void shouldReturnCurrentCartDTO() {
        CartDTO dto = cartService.getCurrentCartDTO(user.getId());
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(user.getCart().getId());
    }

    @Test
    void shouldFindOwnerByName() {
        Optional<User> found = cartService.findOwnerByName("pepe");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pepe");
    }

    @Test
    void shouldAddItemToCartSuccessfully() {
        CartDTO dto = cartService.addItemToCart("pepe", cheese.getId(), 2);
        assertThat(dto).isNotNull();
        assertThat(user.getCart().getItems()).hasSize(1);
        assertThat(user.getCart().getTotalPrice()).isGreaterThan(0);
        assertThat(user.getCart().getTotalWeight()).isGreaterThan(0);
    }

    @Test
    void shouldThrowWhenBoxesIsZero() {
        assertThatThrownBy(() -> cartService.addItemToCart("pepe", cheese.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void shouldThrowWhenNotEnoughBoxes() {
        assertThatThrownBy(() -> cartService.addItemToCart("pepe", cheese.getId(), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough boxes");
    }

    @Test
    void shouldRemoveItemFromCartSuccessfully() {
        // Primero añadimos un item
        cartService.addItemToCart("pepe", cheese.getId(), 1);
        Cart cart = userRepository.findById(user.getId()).get().getCart();
        OrderItem item = cart.getItems().get(0);

        CartDTO dto = cartService.removeItemFromCart(user.getId(), item.getId());
        assertThat(dto).isNotNull();
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
        assertThat(cart.getTotalWeight()).isEqualTo(0.0);
    }
}
