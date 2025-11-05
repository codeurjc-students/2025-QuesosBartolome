package es.codeurjc.quesosbartolome.service;

import java.io.IOException;
import java.net.URISyntaxException;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import jakarta.annotation.PostConstruct;

@Service 
public class DataBaseInitializer {

    @Autowired
    private CheeseRepository cheeseRepository;

    @PostConstruct
    public void init() throws IOException, URISyntaxException {
        // Create Cheese 1
        Cheese semicurado = new Cheese();
        semicurado.setName("Semicurado");
        semicurado.setPrice(12.50);
        semicurado.setDescription("Aged Spanish cheese made from goat's milk.");
        semicurado.setManufactureDate(Date.valueOf(LocalDate.now().minusMonths(2)));
        semicurado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(10)));
        semicurado.setType("Hard");
        semicurado.setImage(null); 

        // Create Cheese 2
        Cheese azul = new Cheese();
        azul.setName("Azul");
        azul.setPrice(15.00);
        azul.setDescription("Soft French cheese with a creamy texture and mild flavor.");
        azul.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(3)));
        azul.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(2)));
        azul.setType("Soft");
        azul.setImage(null);

        // Create Cheese 3
        Cheese Curado = new Cheese();
        Curado.setName("Curado"); 
        Curado.setPrice(17.50); 
        Curado.setDescription("A Spanish **semi-cured** cheese, firm texture, and a pleasant nutty, slightly tangy flavor.");
        Curado.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(5))); 
        Curado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(3)));
        Curado.setType("Semi-Hard"); 
        Curado.setImage(null);



        // Create Cheese 4
        Cheese Chevrett = new Cheese();
        Chevrett.setName("Chevrett"); 
        Chevrett.setPrice(20.00); 
        Chevrett.setDescription("A luxurious **French goat cheese** (chèvre), with a crumbly texture and a distinctive, earthy, and tangy flavor.");
        Chevrett.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(2)));
        Chevrett.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(1).plusWeeks(1)));
        Chevrett.setType("Goat"); 
        Chevrett.setImage(null);

        // Save to DB
        cheeseRepository.save(semicurado);
        cheeseRepository.save(azul);
        cheeseRepository.save(Curado);
        cheeseRepository.save(Chevrett);
    }
}
