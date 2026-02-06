package es.codeurjc.quesosbartolome.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Long cheeseId;
    private String cheeseName;
    private Double cheesePrice;

    @ElementCollection
    private List<Double> boxes;
    private Double weight;
    private Double totalPrice;

    // CONSTRUCTORS
    public OrderItem() {
    }

    // To Cart
    public OrderItem(Cart cart, Long cheeseId, String cheeseName, Double cheesePrice, List<Double> boxes, Double weight,
            Double totalPrice) {
        this.cart = cart;
        this.cheeseId = cheeseId;
        this.cheeseName = cheeseName;
        this.cheesePrice = cheesePrice;
        this.boxes = new ArrayList<>(boxes);
        this.weight = weight;
        this.totalPrice = totalPrice;
    }

    // To Order
    public OrderItem(Order order, Long cheeseId, String cheeseName, Double cheesePrice, List<Double> boxes,
            Double weight, Double totalPrice) {
        this.order = order;
        this.cheeseId = cheeseId;
        this.cheeseName = cheeseName;
        this.cheesePrice = cheesePrice;
        this.boxes = new ArrayList<>(boxes);
        this.weight = weight;
        this.totalPrice = totalPrice;
    }

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getCheeseId() {
        return cheeseId;
    }

    public void setCheeseId(Long cheeseId) {
        this.cheeseId = cheeseId;
    }

    public String getCheeseName() {
        return cheeseName;
    }

    public void setCheeseName(String cheeseName) {
        this.cheeseName = cheeseName;
    }

    public Double getCheesePrice() {
        return cheesePrice;
    }

    public void setCheesePrice(Double cheesePrice) {
        this.cheesePrice = cheesePrice;
    }

    public List<Double> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<Double> boxes) {
        this.boxes = boxes;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

}
