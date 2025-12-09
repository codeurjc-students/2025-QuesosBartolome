package es.codeurjc.quesosbartolome.model;

import java.sql.Blob;
import java.sql.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;

@Entity
public class Cheese {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private double price;

    @Column(length = 1000)
    private String description;
    private Date manufactureDate;
    private Date expirationDate;
    private String Type;
    @ElementCollection
    private List<Double> boxes;

    @Lob
    private Blob image;
    
    @OneToMany(mappedBy = "cheese")
    private List<Review> reviews;
    


    // Constructor
    public Cheese() {

    }
    
    public Cheese(Long id, String name, double price, String description, String type, String manufactureDate, String expirationDate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.Type = type;
        this.manufactureDate = Date.valueOf(manufactureDate);
        this.expirationDate = Date.valueOf(expirationDate);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(Date manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
    
    public List<Double> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<Double> boxes) {
        this.boxes = boxes;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}