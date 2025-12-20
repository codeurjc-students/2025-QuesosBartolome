package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.dto.OrderMapper;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.OrderService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrdersReturnsMappedPage() {
        Order o1 = new Order();
        o1.setId(1L);
        Order o2 = new Order();
        o2.setId(2L);

        Page<Order> page = new PageImpl<>(List.of(o1, o2));
        when(orderRepository.findByOrderDateNotNull(any(Pageable.class))).thenReturn(page);

        Page<OrderDTO> result = orderService.getAllOrders(Pageable.unpaged());

        assertThat(result).hasSize(2);
        assertThat(result.map(OrderDTO::id).toList()).contains(1L, 2L);
        verify(orderRepository).findByOrderDateNotNull(any(Pageable.class));
    }

    @Test
    void findOwnerByNameReturnsEmptyWhenNotFound() {
        when(userRepository.findByName("ghost")).thenReturn(Optional.empty());

        Optional<User> result = orderService.findOwnerByName("ghost");

        assertThat(result).isEmpty();
        verify(userRepository).findByName("ghost");
    }

    @Test
    void findOwnerByNameReturnsUserWhenFound() {
        User user = new User();
        user.setName("pepe");
        when(userRepository.findByName("pepe")).thenReturn(Optional.of(user));

        Optional<User> result = orderService.findOwnerByName("pepe");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("pepe");
        verify(userRepository).findByName("pepe");
    }

    @Test
    void confirmOrderThrowsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.confirmOrder(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void confirmOrderThrowsWhenCartIsNullOrEmpty() {
        User user = new User();
        user.setId(1L);
        user.setCart(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");

        Cart emptyCart = new Cart();
        emptyCart.setItems(new ArrayList<>());
        user.setCart(emptyCart);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.confirmOrder(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void confirmOrderCreatesOrderSuccessfully() {
        // GIVEN
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setTotalWeight(5.0);
        cart.setTotalPrice(50.0);

        Cheese cheese = new Cheese();
        cheese.setId(10L);
        OrderItem cartItem = new OrderItem();
        cartItem.setCheese(cheese);
        cartItem.setWeight(2.0);
        cartItem.setPrice(20.0);
        cart.getItems().add(cartItem);

        User user = new User();
        user.setId(1L);
        user.setCart(cart);
        user.setOrders(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(99L);
            return o;
        });

        // WHEN
        OrderDTO dto = orderService.confirmOrder(1L);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(99L);
        assertThat(user.getOrders()).hasSize(1);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
        assertThat(cart.getTotalWeight()).isEqualTo(0.0);

        verify(orderRepository).save(any(Order.class));
        verify(userRepository).save(user);
    }
}
