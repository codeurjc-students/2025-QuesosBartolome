package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.CartDTO;
import es.codeurjc.quesosbartolome.dto.CartMapper;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CartRepository;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.CartService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheeseRepository cheeseRepository;

    @Spy
    private CartMapper cartMapper = Mappers.getMapper(CartMapper.class);

    @InjectMocks
    private CartService cartService;

    @Test
    void getCurrentCartDTOThrowsWhenUserNotFound() {
        // User not found in repository
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCurrentCartDTO(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getCurrentCartDTOReturnsNullWhenCartIsNull() {
        User user = new User();
        user.setId(1L);
        user.setCart(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CartDTO result = cartService.getCurrentCartDTO(1L);

        assertThat(result).isNull();
        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentCartDTOReturnsDTOWhenCartExists() {
        Cart cart = new Cart();
        cart.setId(10L);
        User user = new User();
        user.setId(1L);
        user.setCart(cart);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CartDTO result = cartService.getCurrentCartDTO(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        verify(userRepository).findById(1L);
    }

    @Test
    void findOwnerByNameReturnsEmptyWhenNotFound() {
        when(userRepository.findByName("nope")).thenReturn(Optional.empty());

        Optional<User> result = cartService.findOwnerByName("nope");

        assertThat(result).isEmpty();
        verify(userRepository).findByName("nope");
    }

    @Test
    void findOwnerByNameReturnsUserWhenFound() {
        User user = new User();
        user.setName("pepe");
        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));

        Optional<User> result = cartService.findOwnerByName("pepe");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("pepe");
        verify(userRepository).findByName("pepe");
    }

    @Test
    void addItemToCartThrowsWhenUserNotFound() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart("ghost", 1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addItemToCartThrowsWhenCartIsNull() {
        User user = new User();
        user.setName("pepe");
        user.setCart(null);
        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> cartService.addItemToCart("pepe", 1L, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart not initialized");
    }

    @Test
    void addItemToCartThrowsWhenCheeseNotFound() {
        Cart cart = new Cart();
        User user = new User();
        user.setName("pepe");
        user.setCart(cart);
        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart("pepe", 1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cheese not found");
    }

    @Test
    void addItemToCartThrowsWhenBoxesIsZeroOrNegative() {
        Cart cart = new Cart();
        User user = new User();
        user.setName("pepe");
        user.setCart(cart);
        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(List.of(1.0, 2.0));
        cheese.setPrice(10.0);

        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        assertThatThrownBy(() -> cartService.addItemToCart("pepe", 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void addItemToCartThrowsWhenNotEnoughBoxes() {
        Cart cart = new Cart();
        User user = new User();
        user.setName("pepe");
        user.setCart(cart);
        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(List.of(1.0));
        cheese.setPrice(10.0);

        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        assertThatThrownBy(() -> cartService.addItemToCart("pepe", 1L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough boxes");
    }

    @Test
    void addItemToCartAddsCheeseSuccessfully() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(0.0);
        cart.setTotalPrice(0.0);

        User user = new User();
        user.setName("pepe");
        user.setCart(cart);

        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(new ArrayList<>(List.of(1.0, 2.0, 3.0)));
        cheese.setPrice(10.0);

        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        CartDTO dto = cartService.addItemToCart("pepe", 1L, 2);

        assertThat(dto).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getTotalWeight()).isGreaterThan(0);
        assertThat(cart.getTotalPrice()).isGreaterThan(0);
        verify(cartRepository).save(cart);
        verify(cheeseRepository).save(cheese);
        assertThat(cart.getItems().get(0).getAddedAt()).isNotNull();
    }

    @Test
    void removeItemFromCartThrowsWhenItemNotFound() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        User user = new User();
        user.setId(1L);
        user.setCart(cart);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> cartService.removeItemFromCart(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void removeItemFromCartRemovesSuccessfully() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(10.0);
        cart.setTotalPrice(100.0);

        OrderItem item = new OrderItem();
        item.setId(5L);
        item.setCheeseId(1L);
        item.setBoxes(List.of(1.0, 1.0));
        item.setWeight(2.0);
        item.setTotalPrice(20.0);
        cart.getItems().add(item);

        User user = new User();
        user.setId(1L);
        user.setCart(cart);

        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(new ArrayList<>(List.of(0.5, 0.5)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        CartDTO dto = cartService.removeItemFromCart(1L, 5L);

        assertThat(dto).isNotNull();
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalWeight()).isEqualTo(8);
        assertThat(cart.getTotalPrice()).isEqualTo(80);
        assertThat(cheese.getBoxes()).hasSize(4); // 2 original + 2 returned
        verify(cartRepository).save(cart);
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void addItemToCartSetsAddedAt() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(0.0);
        cart.setTotalPrice(0.0);

        User user = new User();
        user.setName("pepe");
        user.setCart(cart);

        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(new ArrayList<>(List.of(1.0, 2.0, 3.0)));
        cheese.setPrice(10.0);

        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        CartDTO dto = cartService.addItemToCart("pepe", 1L, 2);

        assertThat(dto).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getAddedAt()).isNotNull();
        verify(cartRepository).save(cart);
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void purgeExpiredRemovesExpiredItems() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(2.0);
        cart.setTotalPrice(20.0);

        OrderItem item = new OrderItem();
        item.setId(5L);
        item.setCheeseId(1L);
        item.setBoxes(List.of(1.0, 1.0));
        item.setWeight(2.0);
        item.setTotalPrice(20.0);
        item.setAddedAt(LocalDateTime.now().minusMinutes(16));
        cart.getItems().add(item);

        Cheese cheese = new Cheese();
        cheese.setId(1L);
        cheese.setBoxes(new ArrayList<>(List.of(0.5, 0.5)));

        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));

        cartService.purgeExpiredCartItems();

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalWeight()).isEqualTo(0);
        assertThat(cart.getTotalPrice()).isEqualTo(0);
        assertThat(cheese.getBoxes()).hasSize(4);
        verify(cartRepository).save(cart);
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void purgeExpiredKeepsRecentItems() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(2.0);
        cart.setTotalPrice(20.0);

        OrderItem item = new OrderItem();
        item.setId(6L);
        item.setCheeseId(2L);
        item.setBoxes(List.of(1.0, 1.0));
        item.setWeight(2.0);
        item.setTotalPrice(20.0);
        item.setAddedAt(LocalDateTime.now());
        cart.getItems().add(item);

        when(cartRepository.findAll()).thenReturn(List.of(cart));

        cartService.purgeExpiredCartItems();

        assertThat(cart.getItems()).hasSize(1);
        verify(cartRepository, never()).save(cart);
    }
}
