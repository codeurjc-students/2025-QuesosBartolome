package es.codeurjc.quesosbartolome.controller;

import java.sql.Blob;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CheeseDTO> getCheeseById(@PathVariable Long id) {

        Optional<CheeseDTO> cheeseOptional = cheeseService.findById(id);

        if (cheeseOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  
        }

        return ResponseEntity.ok(cheeseOptional.get());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) throws Exception {

        Optional<CheeseDTO> cheeseOptional = cheeseService.findById(id);

        if (cheeseOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 
        }

        Optional<Blob> imageOpt = cheeseService.getCheeseImageById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Blob blob = imageOpt.get();

        byte[] bytes = blob.getBytes(1, (int) blob.length());

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(bytes);
    }
}
