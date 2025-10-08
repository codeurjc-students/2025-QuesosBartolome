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
        semicurado.setImage(null); // You can later replace this with a Blob if needed

        // Create Cheese 2
        Cheese azul = new Cheese();
        azul.setName("Azul");
        azul.setPrice(15.00);
        azul.setDescription("Soft French cheese with a creamy texture and mild flavor.");
        azul.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(3)));
        azul.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(2)));
        azul.setType("Soft");
        azul.setImage(null);

        // Save to DB
        cheeseRepository.save(semicurado);
        cheeseRepository.save(azul);
    }
}
