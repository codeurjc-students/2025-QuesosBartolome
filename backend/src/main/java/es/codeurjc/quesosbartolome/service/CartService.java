package es.codeurjc.quesosbartolome.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.dto.CartDTO;
import es.codeurjc.quesosbartolome.dto.CartMapper;
import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CartRepository;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheeseRepository cheeseRepository;

    @Autowired
    private CartMapper cartMapper;

    public CartDTO getCurrentCartDTO(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        Cart cart = user.getCart();

        if (cart == null) {
            return null;
        }

        return cartMapper.toDTO(cart);
    }

    public Optional<User> findOwnerByName(String name) {
        Optional<User> user = userRepository.findByName(name);
        if (user.isPresent()) {
            return user;
        } else {
            return Optional.empty();
        }
    }

    public CartDTO addItemToCart(String username, Long cheeseId, int boxes) {

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Cart cart = user.getCart();
        if (cart == null) {
            throw new IllegalStateException("Cart not initialized");
        }

        Cheese cheese = cheeseRepository.findById(cheeseId)
                .orElseThrow(() -> new IllegalArgumentException("Cheese not found"));

        if (boxes <= 0) {
            throw new IllegalArgumentException("Number of boxes must be greater than zero");
        }
        if (boxes > cheese.getBoxes().size()) {
            throw new IllegalArgumentException("Not enough boxes available");
        }

        addCheeseToCart(cart, cheese, boxes);

        return cartMapper.toDTO(cart);
    }

    private void addCheeseToCart(Cart cart, Cheese cheese, int boxes) {

        List<Double> selectedBoxes = cheese.getBoxes().subList(0, boxes);
        double totalWeight = selectedBoxes.stream().mapToDouble(Double::doubleValue).sum();
        double totalPrice = totalWeight * cheese.getPrice();

        // Actualizar cajas del queso
        cheese.setBoxes(new ArrayList<>(
                cheese.getBoxes().subList(boxes, cheese.getBoxes().size())));
        cheeseRepository.save(cheese);

        // Crear item
        OrderItem item = new OrderItem(cart, cheese, totalWeight, totalPrice);
        cart.getItems().add(item);

        cart.setTotalWeight(cart.getTotalWeight() + totalWeight);
        cart.setTotalPrice(cart.getTotalPrice() + totalPrice);

        cartRepository.save(cart);
    }

    public CartDTO removeItemFromCart(Long userId, Long itemId) {

        User user = userRepository.findById(userId).orElseThrow();
        Cart cart = user.getCart();

        OrderItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        cart.setTotalWeight(cart.getTotalWeight() - item.getWeight());
        cart.setTotalPrice(cart.getTotalPrice() - item.getPrice());

        cart.getItems().remove(item);

        cartRepository.save(cart);

        return cartMapper.toDTO(cart);
    }

}
