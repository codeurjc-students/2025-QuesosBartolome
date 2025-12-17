package es.codeurjc.quesosbartolome.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.CartDTO;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getMyCart(HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = cartService.findOwnerByName(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
        }

        CartDTO cartdto = cartService.getCurrentCartDTO(userOpt.get().getId());

        if (cartdto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();// 404
        }

        return ResponseEntity.ok(cartdto); // 200 OK
    }

    @PutMapping("/addItem")
    public ResponseEntity<CartDTO> addItemToCart(
            HttpServletRequest request,
            @RequestParam Long cheeseId,
            @RequestParam int boxes) {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CartDTO dto = cartService.addItemToCart(principal.getName(), cheeseId, boxes);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/removeItem")
    public ResponseEntity<CartDTO> removeItemFromCart(
            HttpServletRequest request,
            @RequestParam Long itemId) {

        Principal principal = request.getUserPrincipal();
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<User> userOpt = cartService.findOwnerByName(principal.getName());
        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        CartDTO dto = cartService.removeItemFromCart(userOpt.get().getId(), itemId);
        return ResponseEntity.ok(dto);
    }

}
