package es.codeurjc.quesosbartolome.controller;

import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.service.CheeseService;
import es.codeurjc.quesosbartolome.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/cheeses")
public class CheeseRestController {

    @Autowired
    private CheeseService cheeseService;

    @Autowired
    private UserService userService;

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

    @PostMapping("/new")
    public ResponseEntity<CheeseDTO> createCheese(
            @RequestBody CheeseDTO dto,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            System.err.println("ERROR: No authenticated user (principal is null)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!userService.isAdmin(principal.getName())) {
            System.err.println("ERROR: User " + principal.getName() + " is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            System.out.println("Creating cheese with DTO: " + dto);
            CheeseDTO created = cheeseService.createCheese(dto);

            URI location = URI.create("/api/v1/cheeses/" + created.id());
            return ResponseEntity.created(location).body(created);

        } catch (IllegalArgumentException e) {
            System.err.println("ERROR (IllegalArgumentException): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("ERROR (500): " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheeseDTO> updateCheese(
            @PathVariable Long id,
            @RequestBody CheeseDTO dto,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!userService.isAdmin(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CheeseDTO updated = cheeseService.updateCheese(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Void> updateCheeseImage(
            @PathVariable Long id,
            @RequestParam(value = "file", required = true) MultipartFile file,
            HttpServletRequest request) throws Exception {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!userService.isAdmin(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean updated = cheeseService.saveCheeseImage(id, file);

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Void> uploadCheeseImage(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) throws Exception {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!userService.isAdmin(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean updated = cheeseService.saveCheeseImage(id, file);

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheese(
            @PathVariable Long id,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!userService.isAdmin(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = cheeseService.deleteCheese(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}
