package es.codeurjc.quesosbartolome.model;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Cheese cheese;

    private Double weight;       // kg
    private Double price;     // weight * cheese.price

    // CONSTRUCTORS
    public OrderItem() {}

    public OrderItem(Order order, Cheese cheese, Double weight, Double price) {
        this.order = order;
        this.cheese = cheese;
        this.weight = weight;
        this.price = price;
    }

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Cheese getCheese() {
        return cheese;
    }

    public void setCheese(Cheese cheese) {
        this.cheese = cheese;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}
