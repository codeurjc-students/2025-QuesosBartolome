package es.codeurjc.quesosbartolome.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer rating;
    private String comment;

    @ManyToOne
    private User user;  

    @ManyToOne
    private Cheese cheese; 

    public Review() {}

    public Review(Integer rating, String comment, User user, Cheese cheese) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.cheese = cheese;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Cheese getCheese() {
        return cheese;
    }

    public void setCheese(Cheese cheese) {
        this.cheese = cheese;
    }
}
