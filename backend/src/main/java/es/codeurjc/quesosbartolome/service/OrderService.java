package es.codeurjc.quesosbartolome.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.dto.OrderDTO;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.dto.OrderMapper;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderMapper orderMapper;

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findByOrderDateNotNull(pageable);
        return orders.map(orderMapper::toDTO);
    }

    public Optional<User> findOwnerByName(String name) {
        Optional<User> user = userRepository.findByName(name);
        if (user.isPresent()) {
            return user;
        } else {
            return Optional.empty();
        }
    }

    public OrderDTO confirmOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Cart cart = user.getCart();

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order(user);

        for (OrderItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(order, cartItem.getCheeseId(),
                    cartItem.getCheeseName(),
                    cartItem.getCheesePrice(),
                    cartItem.getBoxes(),
                    cartItem.getWeight(),
                    cartItem.getTotalPrice());
            order.getItems().add(orderItem);
        }

        order.setTotalWeight(cart.getTotalWeight());
        order.setTotalPrice(cart.getTotalPrice());

        Order savedOrder = orderRepository.save(order);

        user.getOrders().add(savedOrder);

        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cart.setTotalWeight(0.0);

        userRepository.save(user);

        return orderMapper.toDTO(savedOrder);
    }

}
