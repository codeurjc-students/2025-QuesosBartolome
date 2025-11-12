package es.codeurjc.quesosbartolome.model;

import java.sql.Blob;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String password;
    private String gmail;
    private String direction;
    private String nif;
    private Blob image;
    // Review list 

    // Constructor
    public User() {
    }

    public User(Long id, String name, String password, String gmail, String direction, String nif) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gmail = gmail;
        this.direction = direction;
        this.nif = nif;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public Blob getImage() {
        return image;
    }  
         
    public void setImage(Blob image) {
        this.image = image;
    }

    
}
