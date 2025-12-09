package es.codeurjc.quesosbartolome.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.service.CheeseService;

@RestController
@RequestMapping("/api/v1/cheeses")
public class CheeseRestController {

    @Autowired
    private  CheeseService cheeseService;
    

    @GetMapping
    public List<CheeseDTO> getAllCheeses() {
        return cheeseService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<CheeseDTO> getCheeseById(@PathVariable Long id) {
        return cheeseService.findById(id);
        
    }
}
